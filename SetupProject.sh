mvn archetype:generate -DgroupId=cs.toronto.edu -DartifactId=pgsample -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
cp Main.xml pgsample/pom.xml
cp Main.java pgsample/src/main/java/cs/toronto/edu/
cp Menu.java pgsample/src/main/java/cs/toronto/edu/
rm pgsample/src/main/java/cs/toronto/edu/App.java
# Copy service classes
mkdir -p pgsample/src/main/java/cs/toronto/edu/db
mkdir -p pgsample/src/main/java/cs/toronto/edu/service
mkdir -p pgsample/src/main/java/cs/toronto/edu/model
cp model/FriendsModel.java pgsample/src/main/java/cs/toronto/edu/model/
cp service/AuthService.java pgsample/src/main/java/cs/toronto/edu/service/
cp db/DBConnection.java pgsample/src/main/java/cs/toronto/edu/db/
cp service/FriendsService.java pgsample/src/main/java/cs/toronto/edu/service/
cp service/PortfolioService.java pgsample/src/main/java/cs/toronto/edu/service/
cp model/PortfolioModel.java pgsample/src/main/java/cs/toronto/edu/model/
cp model/StockModel.java pgsample/src/main/java/cs/toronto/edu/model/