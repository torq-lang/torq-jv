```
glenn@glenn-07:~$ taskset -c 16-31 wrk -t8 -c8 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  8 threads and 8 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     2.70ms    6.96ms 122.58ms   98.87%
    Req/Sec   493.21     95.76   636.00     77.53%
  38948 requests in 10.01s, 2.02GB read
Requests/sec:   3891.82
Transfer/sec:    207.18MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c16 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  16 threads and 16 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.49ms    1.03ms  10.13ms   70.92%
    Req/Sec   287.62     18.60   363.00     73.25%
  45853 requests in 10.01s, 2.38GB read
Requests/sec:   4581.13
Transfer/sec:    243.88MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c31 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  16 threads and 31 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.48ms    1.05ms  10.59ms   71.56%
    Req/Sec   288.28     19.29   360.00     72.44%
  45962 requests in 10.01s, 2.39GB read
Requests/sec:   4592.09
Transfer/sec:    244.46MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c32 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  16 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.10ms    3.44ms  29.45ms   82.58%
    Req/Sec   252.76     44.79   333.00     68.62%
  40308 requests in 10.01s, 2.10GB read
Requests/sec:   4026.89
Transfer/sec:    214.37MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c48 -d10s --timeout 10s http://localhost:8080/api/orders
Running 10s test @ http://localhost:8080/api/orders
  16 threads and 48 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    18.95ms   13.76ms  90.32ms   81.91%
    Req/Sec   175.80     96.55     1.25k    59.99%
  28071 requests in 10.10s, 1.46GB read
Requests/sec:   2779.44
Transfer/sec:    147.96MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c32 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.16ms    3.57ms  31.97ms   84.30%
    Req/Sec   252.26     47.11   360.00     71.93%
  241331 requests in 1.00m, 12.55GB read
Requests/sec:   4019.70
Transfer/sec:    213.99MB
Running: command not found
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c33 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 33 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.42ms    3.82ms  34.04ms   84.18%
    Req/Sec   244.41     50.23   353.00     75.05%
  233801 requests in 1.00m, 12.15GB read
Requests/sec:   3894.29
Transfer/sec:    207.31MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c34 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 34 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.25ms    3.62ms  34.30ms   84.50%
    Req/Sec   248.65     47.42   343.00     76.06%
  237865 requests in 1.00m, 12.37GB read
Requests/sec:   3962.17
Transfer/sec:    210.93MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c35 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 35 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.36ms    3.83ms  32.44ms   84.87%
    Req/Sec   245.70     52.04   343.00     72.26%
  235041 requests in 1.00m, 12.22GB read
Requests/sec:   3915.02
Transfer/sec:    208.42MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c36 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 36 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.27ms    3.76ms  32.31ms   85.22%
    Req/Sec   247.85     50.18   333.00     72.49%
  237093 requests in 1.00m, 12.33GB read
Requests/sec:   3949.32
Transfer/sec:    210.24MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c38 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 38 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.47ms    4.02ms  32.72ms   85.14%
    Req/Sec   240.89     53.76   343.00     73.96%
  230450 requests in 1.00m, 11.98GB read
Requests/sec:   3838.51
Transfer/sec:    204.34MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c48 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 48 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    17.94ms   12.80ms  82.23ms   83.06%
    Req/Sec   186.18     86.08   340.00     55.09%
  178164 requests in 1.00m, 9.26GB read
Requests/sec:   2967.05
Transfer/sec:    157.95MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c50 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 50 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    18.31ms   13.21ms  82.10ms   82.94%
    Req/Sec   181.15     84.10   343.00     52.52%
  173350 requests in 1.00m, 9.01GB read
Requests/sec:   2887.38
Transfer/sec:    153.71MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c64 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 64 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    39.22ms   32.35ms 152.55ms   79.96%
    Req/Sec   117.62     96.98   340.00     76.14%
  112598 requests in 1.00m, 5.85GB read
Requests/sec:   1875.41
Transfer/sec:     99.84MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c72 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 72 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    40.56ms   34.54ms 155.29ms   80.29%
    Req/Sec   110.47     94.08   343.00     77.87%
  105746 requests in 1.00m, 5.50GB read
Requests/sec:   1761.29
Transfer/sec:     93.76MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c80 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 80 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   112.71ms   73.97ms 273.22ms   50.57%
    Req/Sec    44.64     53.00   323.00     91.59%
  42547 requests in 1.00m, 2.21GB read
Requests/sec:    708.58
Transfer/sec:     37.72MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c96 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 96 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   228.26ms   87.31ms 426.13ms   77.36%
    Req/Sec    26.91     25.71   320.00     97.22%
  25139 requests in 1.00m, 1.31GB read
Requests/sec:    418.66
Transfer/sec:     22.29MB
glenn@glenn-07:~$ taskset -c 16-31 wrk -t16 -c128 -d1m --timeout 10s http://localhost:8080/api/orders
Running 1m test @ http://localhost:8080/api/orders
  16 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   429.43ms  214.72ms 899.29ms   65.37%
    Req/Sec    21.02     26.67   333.00     95.07%
  17770 requests in 1.00m, 0.92GB read
Requests/sec:    295.90
Transfer/sec:     15.75MB
```

The following rate governed tests maintain their rates. Increasing the `/orders` rate above 3,000 causes thrashing.

Summary:
- 768 total connections
- 64 total threads
- 19,500 transactions per second

```
taskset -c 16-31 wrk2 -t16 -c192 -d2m -R5500 --timeout 10s http://localhost:8080/api/customers
Running 2m test @ http://localhost:8080/api/customers
  16 threads and 192 connections
  Thread calibration: mean lat.: 1.203ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.245ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.212ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.219ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.226ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.258ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.252ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.268ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.249ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.251ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.228ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.247ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.251ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.231ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.261ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.272ms, rate sampling interval: 10ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.41ms  754.25us   8.18ms   71.55%
    Req/Sec   362.96     91.96   666.00     77.90%
  659947 requests in 2.00m, 5.17GB read
Requests/sec:   5499.41
Transfer/sec:     44.15MB

taskset -c 16-31 wrk2 -t16 -c192 -d2m -R5500 --timeout 10s http://localhost:8080/api/employees
Running 2m test @ http://localhost:8080/api/employees
  16 threads and 192 connections
  Thread calibration: mean lat.: 1.224ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.209ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.216ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.224ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.219ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.212ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.193ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.188ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.181ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.198ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.184ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.191ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.194ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.186ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.182ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.191ms, rate sampling interval: 10ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.29ms  719.07us   9.92ms   72.37%
    Req/Sec   362.54     93.52   666.00     77.51%
  659956 requests in 2.00m, 2.64GB read
Requests/sec:   5499.48
Transfer/sec:     22.55MB

taskset -c 16-31 wrk2 -t16 -c192 -d2m -R5500 --timeout 10s http://localhost:8080/api/orders/41
Running 2m test @ http://localhost:8080/api/orders/41
  16 threads and 192 connections
  Thread calibration: mean lat.: 1.460ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.416ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.464ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.441ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.426ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.441ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.442ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.448ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.480ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.499ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.512ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.503ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.508ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.486ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.509ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 1.507ms, rate sampling interval: 10ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.58ms    0.98ms  12.48ms   71.34%
    Req/Sec   362.45     97.50   700.00     75.31%
  659951 requests in 2.00m, 406.58MB read
Requests/sec:   5499.43
Transfer/sec:      3.39MB

taskset -c 16-31 wrk2 -t16 -c192 -d2m -R3000 --timeout 10s http://localhost:8080/api/orders
Running 2m test @ http://localhost:8080/api/orders
  16 threads and 192 connections
  Thread calibration: mean lat.: 2.743ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 2.663ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 2.784ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 2.704ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 2.722ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 2.635ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.404ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.312ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.383ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.304ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.414ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.413ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.414ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.289ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.321ms, rate sampling interval: 10ms
  Thread calibration: mean lat.: 3.281ms, rate sampling interval: 10ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.16ms    1.05ms  13.33ms   67.56%
    Req/Sec   197.68     57.90   444.00     70.04%
  360016 requests in 2.00m, 18.72GB read
Requests/sec:   2999.99
Transfer/sec:    159.70MB
```

Run the server

```
taskset -c 0-15 java -XX:+UseZGC -p ~/.torq/lib -m org.torqlang.examples/org.torqlang.examples.NorthwindServer
```

The following commands were used after rate limiting was added. The rate limiting worked as expected, restricting
orders to just 2,000 requests per second while restricting the others collectively to just 10,000 requests per second.
Note that 32 threads and connections where used. Using a high number of connections set to 512 for just one wrk2 run
worked, but the limiting was just under the actual limit. I questioned having that many connections when running client
and server on the same machine, so I backed it down to 32 threads and connections for all tests moving forward. We
need to test a high number of connections while running separate client and server over a 10Gb ethernet.
```
taskset -c 16-31 wrk2 -t32 -c32 -d2m -R5500 --timeout 10s http://localhost:8080/api/customers
taskset -c 16-31 wrk2 -t32 -c32 -d2m -R5500 --timeout 10s http://localhost:8080/api/employees
taskset -c 16-31 wrk2 -t32 -c32 -d2m -R5500 --timeout 10s http://localhost:8080/api/orders/41
taskset -c 16-31 wrk2 -t32 -c32 -d2m -R5500 --timeout 10s http://localhost:8080/api/orders
```

Run the server on just 8 cores

```
taskset -c 0-7 java -XX:+UseZGC -p ~/.torq/lib -m org.torqlang.examples/org.torqlang.examples.NorthwindServer
```
