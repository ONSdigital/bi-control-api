# HBase Setup

To install HBase, run the following command:

```shell
brew install hbase
```

Use the following commands to start/stop HBase:

```shell
start-hbase.sh
stop-hbase.sh
```

Firstly, start HBase and the REST API

```shell
start-hbase.sh
hbase rest start -p {portnumber}
```