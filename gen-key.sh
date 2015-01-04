rm -rf ~/.gnupg
if [[ ! -d ~/.gnupg ]]
then
  cat >/tmp/key.txt <<EOF
%echo Generating a basic OpenPGP key
Key-Type: DSA
Key-Length: 1024
Subkey-Type: ELG-E
Subkey-Length: 1024
Name-Real: Neil Ellis
Name-Comment: Build Key For dollar
Name-Email: hello@neilellis.me
Expire-Date: 0
Passphrase: ${GPG_PASSPHRASE}
# Do a commit here, so that we can later print "done" :-)
%commit
%echo done
EOF
    gpg --batch --gen-key /tmp/key.txt
fi