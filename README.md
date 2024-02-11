## Redisson Playground project

This project serves as a playground for experimenting with [Redisson](https://github.com/redisson/redisson) (Redis Java client).
The entire project is driven by test cases, and fundamental features of Redisson are implemented within these test cases for experimental purposes.

### Starting Redis in Docker
Go to `docker` folder and execute command below
```
docker-compose up
```

### Accessing the Redis container and the Redis CLI

Access Redis container
```
docker exec -it redis bash
```

Access Redis CLI (once inside the Redis container)
```
redis-cli
```

### Executing test cases

The test cases located in `src/test/java/blog/yrol/test` (make sure the Redis server is up and running in Docker prior to running any test case).

