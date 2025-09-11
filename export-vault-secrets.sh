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

if test -f "$NAV_VIRKSOMHETSSERTIFIKAT_CREDENTIALS"
then
    echo "Setting virksomhetssertifikat_alias"
    export virksomhetssertifikat_alias="$(cat $NAV_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.alias')"
    echo "Setting virksomhetssertifikat_password"
    export virksomhetssertifikat_password="$(cat $NAV_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.password')"
    echo "Setting virksomhetssertifikat_type"
    export virksomhetssertifikat_type="$(cat $NAV_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.type')"
fi

if test -f "$NAV_VIRKSOMHETSSERTIFIKAT_KEY"
then
    echo "Converting certificate from base64"
    base64 --decode $NAV_VIRKSOMHETSSERTIFIKAT_KEY > ${NAV_VIRKSOMHETSSERTIFIKAT_KEY%.b64}

    echo "Setting virksomhetssertifikat_path"
    export virksomhetssertifikat_path="file://${NAV_VIRKSOMHETSSERTIFIKAT_KEY%.b64}"
fi

if test -f /var/run/secrets/nais.io/vault/gcloud_serviceaccount
then
    echo "Setting GOOGLE_APPLICATION_CREDENTIALS"
    export GOOGLE_APPLICATION_CREDENTIALS=/var/run/secrets/nais.io/vault/gcloud_serviceaccount
fi
