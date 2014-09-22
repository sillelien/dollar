if [[ ! -d ~/.gnupg ]]
then
  cat >key.txt <<EOF
%echo Generating a basic OpenPGP key
Key-Type: DSA
Key-Length: 1024
Subkey-Type: ELG-E
Subkey-Length: 1024
Name-Real: Cazcade Limited
Name-Comment:
Name-Email: support@cazcade.com
Expire-Date: 0
Passphrase: ${GPG_PASSPHRASE}
# Do a commit here, so that we can later print "done" :-)
%commit
%echo done
EOF
    gpg --batch --gen-key key.txt
fi