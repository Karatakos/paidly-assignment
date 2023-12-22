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

**Edit**: I incorrectly solved without a fixed currency limit in mind, it was only later I notcied the currency domain object with 10 fixed currencies. In retrospect, we could optimize by leveraring this.

Based on 1440 min in 24 hrs with cached pairs expiring after 5 mins we will make 1400 / 5 = 288 requests p/ 24 hrs with all 128 currencies in a single request. Since it will take 128 requests to fill the cache, we will make 288 + 128 = **416 requests p/ 24 hrs** and only 288 requests p/ 24 hrs for subsequent days as long as the cache remains active.

#### Algorithm

1. In-memory cache (map) for Rate objects (key: Pair)
1. Try to fetch rate from the cache 
1. If cached rate exists and timestamp < 5min then issue rate from cache
1. Else fetch requested rate + all additional rates in the cache via a single query
1. Calcualte the inverse rate at 1/rate and add both to cache, e.g. USDJPY and JPYUSD
1. Issue rate from cache

## Run and Test

In the forex-mtl directory:

1. Run tests: `sbt test`
1. Pull one-frame image: `docker pull paidyinc/one-frame`
1. Run one-frame locally on port 3000: `docker run -d -p 3000:8080 paidyinc/one-frame`
1. Run service locally on port 8080: `sbt run`

Thanks.