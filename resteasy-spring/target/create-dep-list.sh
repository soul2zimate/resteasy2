# Basic script to iterate local maven repo and create a list of the missing
# GAVs compared to remove repo
MEAD_URL=http://download.lab.bos.redhat.com/brewroot/repos/jb-eap-6-rhel-6-build/latest/maven/

pushd target/dependency
for pomfile in $(find . -name \*.pom ); do
  remote_pom=${MEAD_URL}${pomfile}
  if ! wget --spider --quiet "$remote_pom"; then 
    pom_dirname=`dirname $pomfile`
    echo ${pom_dirname} | sed -e 's#./##' -e 's#\(.*\)/\([^/]*\)/\([^/]*\)$#\1:\2:pom:\3#' -e 's#/#.#g' >> ../dep-list.txt
    echo "${pomfile} added to list"
  fi
done
popd

