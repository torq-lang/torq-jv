
## Sources

https://nitikagarw.medium.com/getting-started-with-wrk-and-wrk2-benchmarking-6e3cdc76555f

## Interesting

https://without.boats/blog/thread-per-core/

https://runs-on.com/benchmarks/aws-ec2-instances/

## lscpu

```
lscpu
Architecture:             x86_64
  CPU op-mode(s):         32-bit, 64-bit
  Address sizes:          48 bits physical, 48 bits virtual
  Byte Order:             Little Endian
CPU(s):                   32
  On-line CPU(s) list:    0-31
Vendor ID:                AuthenticAMD
  Model name:             AMD Ryzen 9 7950X 16-Core Processor
    CPU family:           25
    Model:                97
    Thread(s) per core:   2
    Core(s) per socket:   16
    Socket(s):            1
    Stepping:             2
    CPU(s) scaling MHz:   23%
    CPU max MHz:          5881.0000
    CPU min MHz:          545.0000
```

## BenchNorthwindDb

Each request is a native actor request. There is no Torq program. Each response is a Map<String, Object>.

```
taskset -c 0-7 java -XX:+UseZGC -p ~/.torq_lang/lib -m org.torqlang.examples/org.torqlang.examples.BenchNorthwindDb
NorthwindDb created: {concurrency: 3, read_latency: 0}
BenchNorthwindDb
  Total time: 2,181 millis
  Total reads: 3,100,000
  Millis per read: 0.00070
  Reads per second: 1,421,366.35
BenchNorthwindDb
  Total time: 2,038 millis
  Total reads: 3,100,000
  Millis per read: 0.00066
  Reads per second: 1,521,099.12
BenchNorthwindDb
  Total time: 1,721 millis
  Total reads: 3,100,000
  Millis per read: 0.00056
  Reads per second: 1,801,278.33
BenchNorthwindDb
  Total time: 1,654 millis
  Total reads: 3,100,000
  Millis per read: 0.00053
  Reads per second: 1,874,244.26
BenchNorthwindDb
  Total time: 1,648 millis
  Total reads: 3,100,000
  Millis per read: 0.00053
  Reads per second: 1,881,067.96
BenchNorthwindDb
  Total time: 1,699 millis
  Total reads: 3,100,000
  Millis per read: 0.00055
  Reads per second: 1,824,602.71
BenchNorthwindDb
  Total time: 1,680 millis
  Total reads: 3,100,000
  Millis per read: 0.00054
  Reads per second: 1,845,238.10
BenchNorthwindDb
  Total time: 1,677 millis
  Total reads: 3,100,000
  Millis per read: 0.00054
  Reads per second: 1,848,539.06
BenchNorthwindDb
  Total time: 1,669 millis
  Total reads: 3,100,000
  Millis per read: 0.00054
  Reads per second: 1,857,399.64
BenchNorthwindDb
  Total time: 1,656 millis
  Total reads: 3,100,000
  Millis per read: 0.00053
  Reads per second: 1,871,980.68
```

## BenchNorthwindCustomers

A new Torq actor is created for each request to simulate servicing a REST API request. Each response is a list of 29
customers.

```
taskset -c 0-7 java -XX:+UseZGC -p ~/.torq_lang/lib -m org.torqlang.examples/org.torqlang.examples.BenchNorthwindCustomers
NorthwindDb created: {concurrency: 8, read_latency: 0}
BenchNorthwindCustomers
  Total time: 3,429 millis
  Total reads: 310,000
  Millis per read: 0.01106
  Reads per second: 90,405.37
BenchNorthwindCustomers
  Total time: 3,466 millis
  Total reads: 310,000
  Millis per read: 0.01118
  Reads per second: 89,440.28
BenchNorthwindCustomers
  Total time: 3,474 millis
  Total reads: 310,000
  Millis per read: 0.01121
  Reads per second: 89,234.31
BenchNorthwindCustomers
  Total time: 3,466 millis
  Total reads: 310,000
  Millis per read: 0.01118
  Reads per second: 89,440.28
BenchNorthwindCustomers
  Total time: 3,473 millis
  Total reads: 310,000
  Millis per read: 0.01120
  Reads per second: 89,260.01
BenchNorthwindCustomers
  Total time: 3,459 millis
  Total reads: 310,000
  Millis per read: 0.01116
  Reads per second: 89,621.28
BenchNorthwindCustomers
  Total time: 3,472 millis
  Total reads: 310,000
  Millis per read: 0.01120
  Reads per second: 89,285.71
BenchNorthwindCustomers
  Total time: 3,479 millis
  Total reads: 310,000
  Millis per read: 0.01122
  Reads per second: 89,106.06
BenchNorthwindCustomers
  Total time: 3,462 millis
  Total reads: 310,000
  Millis per read: 0.01117
  Reads per second: 89,543.62
BenchNorthwindCustomers
  Total time: 3,465 millis
  Total reads: 310,000
  Millis per read: 0.01118
  Reads per second: 89,466.09
```

## PERFORMANCE PROBLEM

### Server

```
taskset -c 0-15 java -XX:+UseZGC -p ~/.torq_lang/lib -m org.torqlang.examples/org.torqlang.examples.NorthwindServer
```

### 8 = 1,468/sec (HIGHEST)

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t8 -c8 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  8 threads and 8 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.44ms  550.60us   8.27ms   70.13%
    Req/Sec   184.27     12.04   220.00     52.25%
  14699 requests in 10.01s, 764.97MB read
Requests/sec:   1468.84
Transfer/sec:     76.44MB
```

### 16 = 1,327/sec

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c16 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  16 threads and 16 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    12.05ms    0.94ms  21.04ms   75.65%
    Req/Sec    83.00      6.32    90.00     51.69%
  13281 requests in 10.01s, 691.17MB read
Requests/sec:   1327.12
Transfer/sec:     69.07MB
```

### 31 = 1,342/sec

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c31 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  16 threads and 31 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.90ms  848.20us  18.89ms   76.16%
    Req/Sec    83.98      5.80    90.00     51.12%
  13437 requests in 10.01s, 699.29MB read
Requests/sec:   1342.77
Transfer/sec:     69.88MB
```

### 32 = 820/sec (SUDDEN DROP IN PERFORMANCE)

Per thread latency dropped from 11.90 to 38.91

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c32 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  16 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    38.91ms    4.71ms  63.00ms   89.86%
    Req/Sec    51.34      8.67    60.00     75.69%
  8214 requests in 10.01s, 427.47MB read
Requests/sec:    820.76
Transfer/sec:     42.71MB
```

### 48 = 423/sec

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c48 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  16 threads and 48 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   112.79ms    8.36ms 145.20ms   83.21%
    Req/Sec    26.67      6.06    50.00     72.12%
  4234 requests in 10.01s, 220.35MB read
Requests/sec:    422.99
Transfer/sec:     22.01MB
```

### PERFORMANCE PROBLEM SOLVED

We were accidentally using a Java fixed thread pool executor for NorthwindDb sized to the number of "available processors" 32. Now, we are using the AffinityExecutor. This is a surprising difference in performance.

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c32 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    12.41ms    7.26ms 226.43ms   98.83%
    Req/Sec   166.36     14.98   202.00     90.80%
  158843 requests in 1.00m, 8.07GB read
Requests/sec:   2645.66
Transfer/sec:    137.69MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c33 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 33 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.88ms    1.57ms  19.56ms   70.20%
    Req/Sec   168.86      8.82   240.00     55.11%
  161673 requests in 1.00m, 8.22GB read
Requests/sec:   2692.41
Transfer/sec:    140.12MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c34 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 34 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.88ms    1.66ms  19.66ms   69.02%
    Req/Sec   168.78      9.12   202.00     71.50%
  161608 requests in 1.00m, 8.21GB read
Requests/sec:   2691.77
Transfer/sec:    140.09MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c35 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 35 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.92ms    1.63ms  18.94ms   71.20%
    Req/Sec   168.29      9.05   202.00     72.62%
  161133 requests in 1.00m, 8.19GB read
Requests/sec:   2683.83
Transfer/sec:    139.67MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c36 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 36 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.90ms    1.59ms  19.22ms   70.68%
    Req/Sec   168.52      8.86   202.00     73.34%
  161346 requests in 1.00m, 8.20GB read
Requests/sec:   2687.26
Transfer/sec:    139.85MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c37 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 37 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.95ms    1.60ms  19.10ms   71.26%
    Req/Sec   167.88      8.95   202.00     74.73%
  160730 requests in 1.00m, 8.17GB read
Requests/sec:   2677.19
Transfer/sec:    139.33MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c38 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 38 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.96ms    1.62ms  31.12ms   74.85%
    Req/Sec   167.78     10.64   242.00     73.02%
  160634 requests in 1.00m, 8.16GB read
Requests/sec:   2675.23
Transfer/sec:    139.22MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c48 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 48 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    18.01ms    2.06ms  27.57ms   75.65%
    Req/Sec   167.07      9.74   210.00     67.64%
  159958 requests in 1.00m, 8.13GB read
Requests/sec:   2664.25
Transfer/sec:    138.65MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c50 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 50 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    17.98ms    2.22ms  27.25ms   75.81%
    Req/Sec   167.34     10.06   210.00     65.49%
  160215 requests in 1.00m, 8.14GB read
Requests/sec:   2668.49
Transfer/sec:    138.87MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c64 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 64 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    23.76ms    2.24ms  55.52ms   86.34%
    Req/Sec   168.87     13.01   222.00     88.92%
  161682 requests in 1.00m, 8.22GB read
Requests/sec:   2692.53
Transfer/sec:    140.12MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c72 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 72 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    23.88ms    2.61ms  36.84ms   79.62%
    Req/Sec   167.97      9.89   210.00     74.38%
  160815 requests in 1.00m, 8.17GB read
Requests/sec:   2678.68
Transfer/sec:    139.40MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c80 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 80 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    30.06ms    2.98ms  44.56ms   77.69%
    Req/Sec   166.79     11.31   210.00     62.49%
  159692 requests in 1.00m, 8.12GB read
Requests/sec:   2659.88
Transfer/sec:    138.43MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c96 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 96 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    35.96ms    3.50ms  64.25ms   81.66%
    Req/Sec   167.22     12.56   212.00     57.14%
  160107 requests in 1.00m, 8.14GB read
Requests/sec:   2666.73
Transfer/sec:    138.78MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c112 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 112 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    42.06ms    3.34ms  58.47ms   79.25%
    Req/Sec   166.90     13.92   212.00     62.65%
  159783 requests in 1.00m, 8.12GB read
Requests/sec:   2661.32
Transfer/sec:    138.50MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c128 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    47.99ms    4.34ms  88.95ms   83.35%
    Req/Sec   167.15     11.73   240.00     76.27%
  160031 requests in 1.00m, 8.13GB read
Requests/sec:   2665.48
Transfer/sec:    138.72MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c144 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 144 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    53.77ms    3.72ms  96.53ms   83.75%
    Req/Sec   167.81     12.79   222.00     70.36%
  160674 requests in 1.00m, 8.17GB read
Requests/sec:   2676.14
Transfer/sec:    139.27MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c160 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 160 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    59.82ms    3.02ms  92.66ms   77.57%
    Req/Sec   167.64     18.78   202.00     69.58%
  160482 requests in 1.00m, 8.16GB read
Requests/sec:   2672.95
Transfer/sec:    139.11MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c176 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 176 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    65.70ms    3.91ms 100.54ms   79.34%
    Req/Sec   167.91     22.95   222.00     67.54%
  160716 requests in 1.00m, 8.17GB read
Requests/sec:   2676.85
Transfer/sec:    139.31MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c192 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 192 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    72.21ms    4.47ms 124.81ms   82.00%
    Req/Sec   166.65     21.67   242.00     68.76%
  159516 requests in 1.00m, 8.11GB read
Requests/sec:   2656.95
Transfer/sec:    138.27MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c224 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 224 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    82.57ms    5.05ms 147.21ms   86.30%
    Req/Sec   169.94     25.20   272.00     64.39%
  162642 requests in 1.00m, 8.27GB read
Requests/sec:   2708.90
Transfer/sec:    140.98MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c224 -d5m --timeout 10s http://localhost:8080/api/orders
Running 5m test @ http://localhost:8080/api/orders
  16 threads and 224 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    82.80ms    3.54ms 274.32ms   76.03%
    Req/Sec   169.56     23.00   282.00     69.83%
  811161 requests in 5.00m, 41.23GB read
Requests/sec:   2703.26
Transfer/sec:    140.68MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c240 -d5m --timeout 10s http://localhost:8080/api/orders
Running 5m test @ http://localhost:8080/api/orders
  16 threads and 240 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    88.02ms    3.38ms 289.71ms   74.40%
    Req/Sec   170.89     19.77   303.00     55.13%
  817599 requests in 5.00m, 41.55GB read
Requests/sec:   2724.75
Transfer/sec:    141.80MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c368 -d5m --timeout 10s http://localhost:8080/api/orders
Running 5m test @ http://localhost:8080/api/orders
  16 threads and 368 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   134.50ms    5.16ms 344.48ms   77.41%
    Req/Sec   171.49     41.18   232.00     61.26%
  820119 requests in 5.00m, 41.68GB read
Requests/sec:   2733.18
Transfer/sec:    142.24MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c512 -d5m --timeout 10s http://localhost:8080/api/orders
Running 5m test @ http://localhost:8080/api/orders
  16 threads and 512 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   185.42ms    7.51ms 393.48ms   78.27%
    Req/Sec   173.05     80.20   323.00     58.39%
  827502 requests in 5.00m, 42.06GB read
Requests/sec:   2757.61
Transfer/sec:    143.51MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c640 -d5m --timeout 10s http://localhost:8080/api/orders
Running 5m test @ http://localhost:8080/api/orders
  16 threads and 640 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   233.13ms    8.71ms 430.22ms   78.61%
    Req/Sec   172.38     97.59   470.00     57.03%
  822544 requests in 5.00m, 41.80GB read
Requests/sec:   2740.98
Transfer/sec:    142.65MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c768 -d5m --timeout 10s http://localhost:8080/api/orders
Running 5m test @ http://localhost:8080/api/orders
  16 threads and 768 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   279.78ms   11.83ms 489.26ms   82.06%
    Req/Sec   174.32    100.44   474.00     59.60%
  821657 requests in 5.00m, 41.76GB read
Requests/sec:   2738.09
Transfer/sec:    142.50MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c896 -d5m --timeout 10s http://localhost:8080/api/orders
Running 5m test @ http://localhost:8080/api/orders
  16 threads and 896 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   330.05ms   20.74ms   1.33s    91.49%
    Req/Sec   176.05    108.20   545.00     61.13%
  812927 requests in 5.00m, 41.31GB read
Requests/sec:   2708.87
Transfer/sec:    140.98MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c1000 -d5m --timeout 10s http://localhost:8080/api/orders
Running 5m test @ http://localhost:8080/api/orders
  16 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   357.51ms   20.53ms 991.37ms   89.98%
    Req/Sec   180.74    123.36   580.00     60.22%
  830184 requests in 5.00m, 42.19GB read
Requests/sec:   2766.39
Transfer/sec:    143.97MB
```

### Order 41

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c1000 -d5m --timeout 10s http://localhost:8080/api/orders/41
Running 5m test @ http://localhost:8080/api/orders/41
  16 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     7.40ms    5.18ms 421.09ms   55.11%
    Req/Sec     8.45k   325.90    18.52k    79.56%
  40375717 requests in 5.00m, 23.88GB read
Requests/sec: 134540.83
Transfer/sec:     81.48MB
```

### Order 42 Details

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c1000 -d5m --timeout 10s http://localhost:8080/api/orders/42/details
Running 5m test @ http://localhost:8080/api/orders/42/details
  16 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    28.29ms   19.65ms   1.71s    98.28%
    Req/Sec     2.23k   109.10     3.30k    89.96%
  10639158 requests in 5.00m, 18.76GB read
Requests/sec:  35457.97
Transfer/sec:     64.01MB
```

### Employees

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c1000 -d5m --timeout 10s http://localhost:8080/api/employees
Running 5m test @ http://localhost:8080/api/employees
  16 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    16.00ms   11.67ms 449.23ms   50.34%
    Req/Sec     3.90k   204.21    13.83k    81.00%
  18627724 requests in 5.00m, 74.56GB read
Requests/sec:  62071.96
Transfer/sec:    254.43MB
```

### Customers 

```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c1000 -d5m --timeout 10s http://localhost:8080/api/customers
Running 5m test @ http://localhost:8080/api/customers
  16 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    33.12ms   26.70ms 891.00ms   42.63%
    Req/Sec     1.88k   153.06     4.58k    76.62%
  8998013 requests in 5.00m, 70.54GB read
Requests/sec:  29985.76
Transfer/sec:    240.73MB
```
