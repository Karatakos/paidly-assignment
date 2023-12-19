# Submission README

## Key Problem

How do we handle <= 10,000 requests with the constraint of <= 1,000 requests for one-frame

### Assumptions
1. There are only 128 currency pairs
1. URL character limit of ~2,000 allows us to craft a single GET request for all 128 pairs 
1. One-frame service allows such a large GET request
1. Pairs can be inverted by calculating 1/rate so we do not need to make 128 * 2 requests
1. We can get away with an in memory cache such as a map but should design to plugin Redis, Elasticache, etc.

Assumptions validated via a couple of spikes.

### Solution 

Based on 1440 min in 24 hrs with cached pairs expiring after 5 mins we will make 1400 / 5 = 288 requests p/ 24 hrs with all 128 currencies in a single request. Since it will take 128 requests to fill the cache, we will make 288 + 128 = **416 requests p/ 24 hrs** and only 288 requests p/ 24 hrs for subsequent days as long as the cache remains active.

#### Algorithm

1. In-memory cache (map) for Rate objects (key: Pair)
1. Try to fetch rate from the cache 
1. If cached rate exists and timestamp < 5min then issue rate from cache
1. Else fetch requested rate + all additional rates in the cache via a single query
1. Calcualte the inverse rate at 1/rate and add both to cache, e.g. USDJPY and JPYUSD
1. Issue rate from cache

## Issues & Design Decisions

1. I incorreclty designed without a fixed constraint on currencies, i.e. if we get a rate from one-frame it's valid. Only late on in the assignment did I notice Currency.scala.
1. It was not easy at first figuring out what level of abstraction goes where for this pattern. After some research it looks like Tagless Final and so that's where i focused -- treating DSL's similar to repos and Program's similar to commands from CQRS. It was a choice between a single service which is cache aware, or two services composed by the program. The later seemed more logical so cache implementation could switch out for a distributed cache like Redis or Elasticache in production.

## Run and Test

In the forex-mtl directory:

1. Run tests: `sbt test`
1. Pull one-frame image: `docker pull paidyinc/one-frame`
1. Run one-frame locally on port 3000: `docker run -d -p 3000:8080 paidyinc/one-frame`
1. Run service locally on port 8080: `sbt run`

Thanks.