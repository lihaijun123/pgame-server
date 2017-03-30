rem init
call new cmd
call mvn clean
call mvn eclipse:clean
call mvn  package -Dmaven.test.skip=true
call mvn  eclipse:eclipse

echo .... & pause