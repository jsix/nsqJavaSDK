## 2.3.1
### 2.3.1M1
+ Apply ConfigAccessAgent to integrate lookup address and topic trace access to DCC.[#48](http://gitlab.qima-inc.com/paas/nsq-client-java/merge_requests/48)
+ Fluent API in NSQConfig [#49](http://gitlab.qima-inc.com/paas/nsq-client-java/merge_requests/49) and new NSQConfig constructor[#50](http://gitlab.qima-inc.com/paas/nsq-client-java/merge_requests/50)
+ Bug Fixes:
    - UnsupportedOperationException in remove Array.asList() temp list in update lookup address.[#25](http://gitlab.qima-inc.com/paas/nsq-client-java/issues/25)
    - Remove SO_TIMEOUT in netty bootstrap config.[#29](http://gitlab.qima-inc.com/paas/nsq-client-java/issues/29)
### 2.3.1M2
+ Topic partition refactor, user allow to specify partition sharding ID to specify partition in NSQd.[#51](http://gitlab.qima-inc.com/paas/nsq-client-java/merge_requests/51)
+ Connection pool size property in NSQConfig. New property for specifying connection pool size in producer.[#52](http://gitlab.qima-inc.com/paas/nsq-client-java/merge_requests/52)
+ Message meta data checker in consumer, in subscribe order mode.[#53](http://gitlab.qima-inc.com/paas/nsq-client-java/merge_requests/53)
+ Merge partition node and producers node in lookup response.[#55](http://gitlab.qima-inc.com/paas/nsq-client-java/merge_requests/55)
### 2.3.1M3