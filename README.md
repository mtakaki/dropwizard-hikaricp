# Status
![Build Status](https://codeship.com/projects/37601470-cb27-0133-b906-266ee181f653/status?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/mtakaki/dropwizard-hikaricp/badge.svg?branch=master)](https://coveralls.io/github/mtakaki/dropwizard-hikaricp?branch=master)
[![Download](https://maven-badges.herokuapp.com/maven-central/com.github.mtakaki/dropwizard-hikaricp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mtakaki/dropwizard-hikaricp)

# dropwizard-hikaricp
This library provides a HikariCP integration for dropwizard, instead of using tomcat connection pool.

## Maven

The library is available at the maven central, so just add dependency to `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>com.github.mtakaki</groupId>
    <artifactId>dropwizard-hikaricp</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>
```

## Benchmark

Before looking into the benchmark numbers, I recommend reading [HikariCP wiki](https://github.com/brettwooldridge/HikariCP/wiki/%22My-benchmark-doesn't-show-a-difference.%22) explaining the differences between Hikari and Tomcat.

The benchmark was ran on these specs:

- 1.4 GHz Intel Core i5 CPU
- Connection pool:
    - minimum size: 2
    - maximum size: 5
- 5000 requests
- 50 parallel clients

TomcatCP was used as it is and no additional setting was used to match to HikariCP's reliability.

### Querying

#### Get single entry

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 2.364685 | 2.353321 |
| p75 | 4.830371 | 4.374164 |
| p95 | 21.761829 | 14.106643 |
| p98 | 150.097517 | 188.824843 |
| p99 | 197.370100 | 200.131246 |
| p999 | 317.904803 | 309.082305 |

#### Get multiple entries (5,000 records)

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 27.840070 | 240.507857 |
| p75 | 52.545500 | 852.952037 |
| p95 | 251.368732 | 2,008.345248 |
| p98 | 318.400576 | 2,420.165156 |
| p99 | 355.222567 | 2,585.712459 |
| p999 | 603.264130 | 3,212.936003 |

### Inserting

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 2.791224 | 89.453760 |
| p75 | 6.254607 | 218.588807 |
| p95 | 21.253595 | 350.772996 |
| p98 | 119.653682 | 420.798200 |
| p99 | 135.083573 | 489.772740 |
| p999 | 179.969553 | 555.170417 |

### Updating

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 |  | 11.725880 |
| p75 |  | 38.890317 |
| p95 |  | 290.270407 |
| p98 |  | 338.387440 |
| p99 |  | 399.467055 |
| p999 |  | 542.977120 |

### Deleting

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 10.856938 | 190.813232 |
| p75 | 18.364517 | 265.779441 |
| p95 | 80.736182 | 422.216480 |
| p98 | 117.330967 | 552.820746 |
| p99 | 136.620702 | 659.247842 |
| p999 | 162.919074 | 1,030.488379 |
