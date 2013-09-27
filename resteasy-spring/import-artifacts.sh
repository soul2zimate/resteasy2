mkdir -p target/temp-download
pushd target/temp-download
for ARTIFACT in `cat ../dep-list.txt`; 
do
  echo $ARTIFACT;
  ../../get-maven-artifacts $ARTIFACT; 
  ../../import-maven * --tag=jb-eap-6-rhel-6-imports;
  rm *;
done
popd
