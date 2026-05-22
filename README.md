GRADLE
------
Build the project:
```
./gradlew build
```

Run the backend (starts on http://localhost:8080):
```
./gradlew bootRun
```

Run tests:
```
./gradlew test
```

Run a single test:
```
./gradlew test --tests "com.highspring.shopping.ShoppingApplicationTests"
```

Show the full runtime dependency tree:
```
./gradlew dependencies --configuration runtimeClasspath
```

Clean build output:
```
./gradlew clean
```
