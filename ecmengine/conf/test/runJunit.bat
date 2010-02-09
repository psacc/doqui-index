@echo off

set JAVA_HOME=@@path_jdk_executor_test@@

echo @set JUNIT_JAR=%%JUNIT_JAR%%;%%1>cpappend.bat

set JUNIT_JAR=.
for %%f in (*.jar) do (
   call cpappend %%f
)

java -classpath %JUNIT_JAR% junit.swingui.TestRunner it.doqui.index.ecmengine.test.AllTests -noloading

del cpappend.bat
