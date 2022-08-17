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

if test -f /secrets/virksomhetssertifikat/credentials.json
then
    echo "Setting virksomhetssertifikat_alias"
    export virksomhetssertifikat_alias="$(cat /secrets/virksomhetssertifikat/credentials.json | jq -r '.alias')"
    echo "Setting virksomhetssertifikat_password"
    export virksomhetssertifikat_password="$(cat /secrets/virksomhetssertifikat/credentials.json | jq -r '.password')"
    echo "Setting virksomhetssertifikat_type"
    export virksomhetssertifikat_type="$(cat /secrets/virksomhetssertifikat/credentials.json | jq -r '.type')"
fi
if test -f /secrets/virksomhetssertifikat/key.p12.b64
then
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
