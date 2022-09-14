"""
This is a simple script that writes results from a model hosted via
the openLCA API to a MySQL / MariaDB database. This can be then used
to visualize the results with Grafana. Steps to get this running:

# MariaDB

* get it from https://mariadb.org/download/
* initialize the database and start the server (default user is `root`
  with no password!)

```
cd mariadb\bin
.\mysql_install_db.exe
.\mysqld.exe --console
```

* get a friendly UI tool like HeidiSQL (https://www.heidisql.com/) and
  create the database:

```sql
CREATE DATABASE gdt;
USE gdt;

CREATE TABLE tbl_results (
    time       DOUBLE,
    indicator  VARCHAR(255),
    unit       VARCHAR(255),
    result     DOUBLE
);
```

* finally, install the Python driver for MariaDB
  https://mariadb.com/de/resources/blog/how-to-connect-python-programs-to-mariadb/

```
pip install mariadb
```

* Then you should be able to run this script. It will run a
  calculation for each of the configured iterations and wait a second
  after each run. (But of course you need to start the gdt-server first:
  `java -jar gdt-server.jar -db ./db -lib ./amd64 -port 8080`)

# Grafana

* download the self-managed OSS version from:
  https://grafana.com/grafana/download
* start the Grafana server:

```
cd grafana\bin
.\grafana-server.exe
```

* this starts the server at http://localhost:3000; default user is
  `admin` and password is also `admin`
* add a new MySQL data source via the UI: table `tbl_results`, time
  column `time`, metric column `indicator`, selected column `result`.
* `time` expects a UNIX timestamp in seconds by default
* you can then add a dashboard and use this data source / query in a
  panel
"""

import copy
import time
import math
import random
import requests
import mariadb


# configuration of the endpoint with the openLCA API; the model and LCIA method
ENDPOINT = 'http://localhost:8080'
MODEL = '90e9cabf-2915-40f4-a660-6aaa105d7cc6'
METHOD = '31338be3-5c35-37ba-a597-a3d85dc994a9'

SQL_SCHEMA = '''

CREATE DATABASE gdt;
USE gdt;

CREATE TABLE tbl_results (
    time       DOUBLE,
    indicator  VARCHAR(255),
    unit       VARCHAR(255),
    result     DOUBLE
);

'''

ITERATIONS = 400


def main():

    # connect to database
    conn = mariadb.connect(
        user="root",
        host="localhost",
        port=3306,
        database="gdt")
    cursor = conn.cursor()

    # fetch the model parameters
    param_url = f'{ENDPOINT}/models/{MODEL}/parameters'
    params: list[dict] = requests.get(param_url).json()

    for i in range(0, ITERATIONS):

        # modify the parameter values
        factor = math.sin(i * math.pi / 8) * 0.5
        factor += factor * 0.1 * random.random()
        next_params = []
        for param in params:
            next_param = copy.deepcopy(param)
            if not next_param['name'].startswith('ws_'):
                value: float = next_param['value']
                next_param['value'] = value + factor * value
            next_params.append(next_param)

        # calculate the results
        setup = {
            'productSystem': {'@id': MODEL},
            'impactMethod': {'@id': METHOD},
            'amount': 1.0,
            'parameterRedefs': next_params,
        }
        result: dict = requests.post(
            ENDPOINT + '/calculate', json=setup).json()
        impacts: list[dict] = result.get('impactResults', [])

        # write the results to the database
        for impact in impacts:
            indicator = impact['impactCategory']
            cursor.execute('''
            insert into tbl_results (time, indicator, unit, result)
            values (?, ?, ?, ?)
            ''', (
                time.time(),
                indicator['name'],
                indicator['refUnit'],
                impact['value']))
        conn.commit()
        time.sleep(1)
        print(f'added result {i + 1}')

    conn.close()


if __name__ == '__main__':
    main()
