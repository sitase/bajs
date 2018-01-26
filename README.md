./gradlew clean assemble

java -Xbootclasspath/p:build/libs/bajs-rt.jar -javaagent:build/libs/bajs-agent.jar -jar build/libs/bajs-sample.jar 

java -jar build/libs/bajs-sample-boot.jar

java -Xbootclasspath/p:build/libs/bajs-rt.jar -javaagent:build/libs/bajs-agent.jar -jar build/libs/bajs-sample-boot.jar

http://localhost:8080/hello?name=World
http://localhost:8080/hello?name=%3Cscript%3Ealert(%27test%27);%3C/script%3E
