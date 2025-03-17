BidGenerator Performance Test Result
========

[In Chinese 中文版](BIDPerformance_zh.md)

## Test Machine

- MacBook Pro (Retina, 15-inch, Mid 2015)
- Processor: 2.2 GHz Intel Core i7
- Memory: 16 GB 1600 MHz DDR3

## Resource Usage

- With 1 thread, local CPU usage averages 120%-200%, peaking at no more than 300%. MySQL server CPU usage averages 10%-20%, peaking at no more than 50%.
- With 20 threads, local CPU usage averages 500%-700%, peaking at no more than 1200%. MySQL server CPU usage averages 70%-120%, peaking at no more than 130%.

> During multi-threaded testing, MySQL and Redis database CPU usage is relatively low, while the stress test machine (Mac PC) CPU usage is high. For higher thread counts, consider using a higher-specification stress test machine.

## Performance Test Results

| Mode  | Thread/Tenant | Format          | Step  | Sample        | IDs/Second  |
|-------|---------------|-----------------|-------|---------------|-------------|
| DB    | 1             | PREFIX_DATE_SEQ | 1000  | 2000001       | 58747.53    |
| DB    | 1             | PREFIX_DATE_SEQ | 5000  | 2000001       | 127089.08   |
| DB    | 1             | PREFIX_DATE_SEQ | 10000 | 20000001      | 190142.99   |
| DB    | 1             | PREFIX_DATE_SEQ | 20000 | 20000001      | 225068.09   |
| DB    | 1             | PREFIX_SEQ      | 1000  | 2000001       | 82192.94    |
| DB    | 1             | PREFIX_SEQ      | 10000 | 20000001      | 329218.12   |
| DB    | 1             | PREFIX_SEQ      | 20000 | 20000001      | 443085.67   |
| DB    | 1             | DATE_SEQ        | 1000  | 2000001       | 840912.66   |
| DB    | 1             | DATE_SEQ        | 10000 | 20000001      | 186762.29   |
| DB    | 1             | DATE_SEQ        | 20000 | 20000001      | 212332.26   |
| DB    | 1             | SEQ             | 1000  | 2000001       | 89337.60    |
| DB    | 1             | SEQ             | 10000 | 20000001      | 343406.61   |
| DB    | 1             | SEQ             | 20000 | 20000001      | 462791.58   |
| DB    | 5             | PREFIX_DATE_SEQ | 1000  | 2000001  * 5  | 338180.75   |
| DB    | 5             | PREFIX_DATE_SEQ | 10000 | 20000001 * 5  | 354446.70   |
| DB    | 5             | PREFIX_DATE_SEQ | 20000 | 20000001* 5   | 369467.41   |
| DB    | 5             | SEQ             | 1000  | 2000001  * 5  | 641067.05   |
| DB    | 5             | SEQ             | 10000 | 20000001 * 5  | 1098539.49  |
| DB    | 5             | SEQ             | 20000 | 20000001 * 5  | 1186662.51  |
| DB    | 20            | PREFIX_DATE_SEQ | 1000  | 2000001 * 20  | 338180.75   |
| DB    | 20            | PREFIX_DATE_SEQ | 10000 | 20000001 * 20 | 354446.70   |
| DB    | 20            | PREFIX_DATE_SEQ | 20000 | 20000001 * 20 | 369467.41   |
| DB    | 20            | SEQ             | 1000  | 2000001  * 20 | 794088.88   |
| DB    | 20            | SEQ             | 10000 | 20000001 * 20 | 945720.47   |
| DB    | 20            | SEQ             | 20000 | 20000001 * 20 | 1019492.802 |
| Redis | 1             | PREFIX_DATE_SEQ | 1000  | 2000001       | 205697.93   |
| Redis | 1             | PREFIX_DATE_SEQ | 5000  | 2000001       | 206996.58   |
| Redis | 1             | PREFIX_DATE_SEQ | 10000 | 20000001      | 234436.36   |
| Redis | 1             | PREFIX_DATE_SEQ | 20000 | 20000001      | 200801.20   |
| Redis | 1             | PREFIX_SSE      | 1000  | 2000001       | 354861.78   |
| Redis | 1             | PREFIX_SEQ      | 5000  | 2000001       | 433557.55   |
| Redis | 1             | PREFIX_SEQ      | 10000 | 20000001      | 4817900.45  |
| Redis | 1             | PREFIX_SEQ      | 20000 | 20000001      | 514986.12   |
| Redis | 1             | DATE_SEQ        | 1000  | 2000001       | 206526.33   |
| Redis | 1             | DATE_SEQ        | 5000  | 2000001       | 246548.44   |
| Redis | 1             | DATE_SEQ        | 10000 | 20000001      | 233091.08   |
| Redis | 1             | DATE_SEQ        | 20000 | 20000001      | 245428.89   |
| Redis | 1             | SEQ             | 1000  | 2000001       | 419023.88   |
| Redis | 1             | SEQ             | 5000  | 2000001       | 593648.26   |
| Redis | 1             | SEQ             | 10000 | 20000001      | 450734.72   |
| Redis | 1             | SEQ             | 20000 | 20000001      | 505050.53   |
| Redis | 5             | PREFIX_DATE_SEQ | 1000  | 2000001  * 5  | 358705.96   |
| Redis | 5             | PREFIX_DATE_SEQ | 10000 | 20000001 * 5  | 260727.66   |
| Redis | 5             | PREFIX_DATE_SEQ | 20000 | 20000001 * 5  | 364878.31   |
| Redis | 5             | SEQ             | 1000  | 2000001 * 5   | 850253.46   |
| Redis | 5             | SEQ             | 10000 | 20000001 * 5  | 1079284.32  |
| Redis | 5             | SEQ             | 20000 | 20000001 * 5  | 749647.74   |
| Redis | 20            | PREFIX_DATE_SEQ | 1000  | 2000001  * 20 | 367132.90   |
| Redis | 20            | PREFIX_DATE_SEQ | 10000 | 20000001 * 20 | 428610.54   |
| Redis | 20            | PREFIX_DATE_SEQ | 20000 | 20000001 * 20 | 419910.72   |
| Redis | 20            | SEQ             | 1000  | 2000001 * 20  | 890927.08   |
| Redis | 20            | SEQ             | 10000 | 20000001 * 20 | 916145.31   |
| Redis | 20            | SEQ             | 20000 | 20000001 * 20 | 1002692.32  |

