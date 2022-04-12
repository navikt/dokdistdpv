package no.nav.dokdistdpv.cloudstorage;

public interface EncryptedBucketStorage {
	/**
	 * Laster ned og dekrypterer objekt fra Cloud Storage
	 *
	 * @param objectName     Navn på objektet som finnes i bucket. GUID eller annen unik ID som er kjent.
	 * @param associatedData Data som knyttes til objektet for å unngå manipulering. Må ha lik verdi som da det ble kryptert.
	 *                       F.eks journalpostId, bestillingsId.
	 * @return Unmarshalled DokDistDokumentFraBucket i klartekst
	 */
	DokDistDokumentFraBucket downloadObject(String objectName, String associatedData);
}
