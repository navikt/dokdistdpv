#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvdokdistdpv/username;
then
    echo "Setting dokdistdpv_serviceuser_username"
    export dokdistdpv_serviceuser_username=$(cat /secrets/serviceuser/srvdokdistdpv/username)
fi
if test -f /secrets/serviceuser/srvdokdistdpv/password;
then
    echo "Setting dokdistdpv_serviceuser_password"
    export dokdistdpv_serviceuser_password=$(cat /secrets/serviceuser/srvdokdistdpv/password)
fi

echo "Exporting appdynamics environment variables"
if test -f /var/run/secrets/nais.io/appdynamics/appdynamics.env;
then
    export $(cat /var/run/secrets/nais.io/appdynamics/appdynamics.env)
    export APPDYNAMICS_AGENT_BASE_DIR=/tmp/appdynamics
    echo "Appdynamics environment variables exported"
else
    echo "No such file or directory found at /var/run/secrets/nais.io/appdynamics/appdynamics.env"
fi

export new_credentials_2023_path=/secrets/virksomhetssertifikat/credentials_2023.json
export old_credentials_path=/secrets/virksomhetssertifikat/credentials.json

if test -f $new_credentials_2023_path
then
    echo "Setting virksomhetssertifikat_alias"
    export virksomhetssertifikat_alias="$(cat $new_credentials_2023_path | jq -r '.alias')"
    echo "Setting virksomhetssertifikat_password"
    export virksomhetssertifikat_password="$(cat $new_credentials_2023_path | jq -r '.password')"
    echo "Setting virksomhetssertifikat_type"
    export virksomhetssertifikat_type="$(cat $new_credentials_2023_path | jq -r '.type')"
else
    echo "Setting virksomhetssertifikat_alias"
    export virksomhetssertifikat_alias="$(cat $old_credentials_path | jq -r '.alias')"
    echo "Setting virksomhetssertifikat_password"
    export virksomhetssertifikat_password="$(cat $old_credentials_path | jq -r '.password')"
    echo "Setting virksomhetssertifikat_type"
    export virksomhetssertifikat_type="$(cat $old_credentials_path | jq -r '.type')"

fi

if test -f /secrets/virksomhetssertifikat/274258896775237957919470-2023-10-11.p12.b64
then
    echo "Setting virksomhetssertifikat_path"
    export virksomhetssertifikat_path="file:///secrets/virksomhetssertifikat/key.p12"

    echo "Converting certificate from base64"
    base64 --decode /secrets/virksomhetssertifikat/274258896775237957919470-2023-10-11.p12.b64 > /secrets/virksomhetssertifikat/key.p12
else
    echo "Setting virksomhetssertifikat_path"
    export virksomhetssertifikat_path="file:///secrets/virksomhetssertifikat/key.p12"

    echo "Converting certificate from base64"
    base64 --decode /secrets/virksomhetssertifikat/key.p12.b64 > /secrets/virksomhetssertifikat/key.p12
fi

if test -f /var/run/secrets/nais.io/vault/gcloud_serviceaccount
then
    echo "Setting GOOGLE_APPLICATION_CREDENTIALS"
    export GOOGLE_APPLICATION_CREDENTIALS=/var/run/secrets/nais.io/vault/gcloud_serviceaccount
fi

if test -f /var/run/secrets/nais.io/certificate/keystore
then
    echo "Setting DOKDISTDPVCERT_KEYSTORE"
    CERT_PATH='/var/run/secrets/nais.io/certificate/keystore-extracted'
    openssl base64 -d -A -in /var/run/secrets/nais.io/certificate/keystore -out $CERT_PATH
    export DOKDISTDPVCERT_KEYSTORE=$CERT_PATH
fi

if test -f /var/run/secrets/nais.io/certificate/keystorepassword
then
    echo "Setting DOKDISTDPVCERT_KEYSTORE_PASSWORD"
    export DOKDISTDPVCERT_KEYSTORE_PASSWORD=$(cat /var/run/secrets/nais.io/certificate/keystorepassword)
fi