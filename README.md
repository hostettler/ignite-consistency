# ignite-consistency
Trying to understand ignite's consistency when WAL=NONE and persistence=true
Shutdown is set to ``cfg.setShutdownPolicy(ShutdownPolicy.GRACEFUL);``
Off-heap size for the persisted region is set to 250M



Build the project with
```
mvn clean package
```

Execute the test with
```
rm -Rf /tmp/ignite/ # To ensure that everything is clean
mvn exec:exec
```

Waiting until we produce more than at least twice the offheap size of the data region (500MB)

```
Put 1,100,000 entries in job 5
[13:01:02]
[13:01:02]     ^--   sysMemPlc region [type=internal, persistence=true, lazyAlloc=false,
[13:01:02]       ...  initCfg=40MB, maxCfg=100MB, usedRam=0MB, freeRam=99.98%, allocRam=99MB, allocTotal=0MB]
[13:01:02]     ^--   metastoreMemPlc region [type=internal, persistence=true, lazyAlloc=false,
[13:01:02]       ...  initCfg=40MB, maxCfg=100MB, usedRam=0MB, freeRam=99.95%, allocRam=0MB, allocTotal=0MB]
[13:01:02]     ^--   TxLog region [type=internal, persistence=true, lazyAlloc=false,
[13:01:02]       ...  initCfg=40MB, maxCfg=100MB, usedRam=0MB, freeRam=100%, allocRam=99MB, allocTotal=0MB]
[13:01:02]     ^--   main region [type=default, persistence=true, lazyAlloc=true,
[13:01:02]       ...  initCfg=250MB, maxCfg=250MB, usedRam=245MB, freeRam=1.95%, allocRam=249MB, allocTotal=952MB]
[13:01:02]     ^--   volatileDsMemPlc region [type=user, persistence=false, lazyAlloc=true,
[13:01:02]       ...  initCfg=40MB, maxCfg=100MB, usedRam=0MB, freeRam=100%, allocRam=0MB]
[13:01:02]
[13:01:02]
[13:01:02] Data storage metrics for local node (to disable set 'metricsLogFrequency' to 0)
[13:01:02]     ^-- Off-heap memory [used=245MB, free=62.28%, allocated=449MB]
[13:01:02]     ^-- Page memory [pages=62046]
[13:01:02]     ^-- Ignite persistence [used=952MB]
Put 1,200,000 entries in job 6
Put 1,200,000 entries in job 4
Put 1,200,000 entries in job 0
Put 1,200,000 entries in job 2
Put 1,200,000 entries in job 8
Put 1,200,000 entries in job 5
Put 1,200,000 entries in job 3
Put 1,200,000 entries in job 9
Put 1,200,000 entries in job 7
```


When sending a SIGTERM from another terminal then we can see a graceful shutdown `` kill -s SIGTERM PID``  

```
...
Put 1,400,000 entries in job 6
Put 1,400,000 entries in job 4
************* Job cancelled
************* Job cancelled
************* Job cancelled
************* Job cancelled
************* Job cancelled
************* Job cancelled
************* Job cancelled
************* Job cancelled
************* Job cancelled
************* Job cancelled
[13:01:34] Ignite node stopped OK [name=cluster, uptime=00:01:31.945]
```


Then starting again the same process
```
mvn exec:exec
```

results in

```
Dec 13, 2024 1:02:37 PM org.apache.ignite.logger.java.JavaLogger error
SEVERE: Critical system error detected. Will be handled accordingly to configured handler [hnd=StopNodeOrHaltFailureHandler [tryStop=false, timeout=0, super=AbstractFailureHandler [ignoredFailureTypes=UnmodifiableSet [SYSTEM_WORKER_BLOCKED, SYSTEM_CRITICAL_OPERATION_TIMEOUT]]], failureCtx=FailureContext [type=CRITICAL_ERROR, err=class o.a.i.i.processors.cache.persistence.StorageException: Failed to read checkpoint record from WAL, persistence consistency cannot be guaranteed. Make sure configuration points to correct WAL folders and WAL folder is properly mounted [ptr=WALPointer [idx=0, fileOff=0, len=0], walPath=db/wal, walArchive=db/wal/archive]]]
class org.apache.ignite.internal.processors.cache.persistence.StorageException: Failed to read checkpoint record from WAL, persistence consistency cannot be guaranteed. Make sure configuration points to correct WAL folders and WAL folder is properly mounted [ptr=WALPointer [idx=0, fileOff=0, len=0], walPath=db/wal, walArchive=db/wal/archive]
        at org.apache.ignite.internal.processors.cache.persistence.GridCacheDatabaseSharedManager.performBinaryMemoryRestore(GridCacheDatabaseSharedManager.java:2136)
        at org.apache.ignite.internal.processors.cache.persistence.GridCacheDatabaseSharedManager.readMetastore(GridCacheDatabaseSharedManager.java:851)
        at org.apache.ignite.internal.processors.cache.persistence.GridCacheDatabaseSharedManager.notifyMetaStorageSubscribersOnReadyForRead(GridCacheDatabaseSharedManager.java:3064)
        at org.apache.ignite.internal.IgniteKernal.start(IgniteKernal.java:1130)
        at org.apache.ignite.internal.IgnitionEx$IgniteNamedInstance.start0(IgnitionEx.java:1725)
        at org.apache.ignite.internal.IgnitionEx$IgniteNamedInstance.start(IgnitionEx.java:1647)
```
