package no.nav.dokdistdpv.cloudstorage;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.crypto.tink.Aead;

import java.security.GeneralSecurityException;

import static no.nav.dokdistdpv.cloudstorage.JsonSerializer.deserialize;

/**
 * Google Cloud implementasjon av {@link EncryptedBucketStorage}.
 */
public class GoogleCloudEncryptedBucketStorage implements EncryptedBucketStorage {

	private final String bucket;
	private final Storage storage;
	private final Aead aead;

	public GoogleCloudEncryptedBucketStorage(Storage storage, String bucket, Aead aead) {
		this.storage = storage;
		this.bucket = bucket;
		this.aead = aead;
	}

	@Override
	public DokDistDokumentFraBucket downloadObject(String objectName, String associatedData) {
		try {
			byte[] cipherText = storage.readAllBytes(bucket, objectName);
			byte[] plainText = aead.decrypt(cipherText, associatedData.getBytes());

			DokDistDokumentFraBucket dokDistDokumentFraBucket = deserialize(new String(plainText), DokDistDokumentFraBucket.class);
			return dokDistDokumentFraBucket.withObjectName(objectName);
		} catch (GeneralSecurityException | StorageException e) {
			throw new ObjectDownloadFailedException(String.format("Teknisk feil mot Google Cloud Storage ved henting på objectName=%s. Feilmelding=%s",
					objectName, e.getMessage()), e);
		} catch (IllegalStateException e) {
			throw new ObjectDownloadFailedException(String.format("Klarte ikke unmarshalle objectName=%s etter henting fra Google Cloud Storage og dekryptering. Feilmelding=%s",
					objectName, e.getMessage()), e);
		}
	}
}
