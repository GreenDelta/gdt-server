# gdt-server

`gdt-server` is a small server that provides a Rest-API for some openLCA core
functions like data management and calculations. It can be integrated as a
service in a larger application context.

## Running the server

The server is a Java application that connects to an openLCA database. It can
be configured by providing the following command line arguments:

```

gdt-server

Usage:

  java -jar gdt-server.jar <args>

Arguments:

  -data <path to data folder>

    Path to the data folder that contains the database, possible libraries etc.
    The folder structure should follow the openLCA workspace structure (with the
    databases and libraries in the respective sub-folders). If this option is
    not provided, it defaults to the respective default openLCA workspace
    location: ~/openLCA-data-1.4

  -db <database>

    The name of the database. This is the name of the respective folder in the
    data directory. This argument is required.

  -port <port>

    The port of the server. Defaults to 8080 if this option is not provided.

  -native <path to native library folder>

    The path to the folder from which the native libraries should be loaded.
    Defaults to the data folder if this path is not provided.

  -static <path to folder with static files>

    An optional path to a folder with static files that should be hosted by
    the server.

  --readonly <true | false>?

    If this flag is set, the server will run in readonly mode and modifying the
    database will not be possible via http requests.

```

## Running as Docker container

When you run the server as Docker container, you need to mount a workspace
folder that contains the database and possible libraries to the `/app/data`
folder of the container. The name of the database and possible other arguments
are passed to the container at the end of the command:

```batch
sudo docker run \
  -p 8080:8080 \
  -v $HOME/openLCA-data-1.4:/app/data \
  --rm -d gdt-server \
  -db ei22 --readonly
```

You can also host static files by mounting a folder to the `/app/static` folder:

```
  -v <your host folder>:/app/static
```

## TODO:

* clean-up old results; e.g. when a new result is cached, check for old results
  that were not used for a specific time and remove them from the map
* allow put with JSON arrays: for inserting multiple data of a respective type

## API

### `GET /locations`
* returns the descriptors of the available locations
* each descriptor is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
[
  {
    "@type": "Location",
    "@id": "b351bb9b-0af6-34fc-a787-49675c53ad67",
    "name": "Madagascar",
    "description": "reference location, sources: ISO 3166-1, ecoinvent 3, ILCD, GaBi",
    "version": "00.00.000"
  }
]
```

### `GET /locations/{id}`
* returns the full location with the given ID
* response is of type [Location](http://greendelta.github.io/olca-schema/Location.html)
* example response:

```json
{
  "@context": "http://greendelta.github.io/olca-schema/context.jsonld",
  "@type": "Location",
  "@id": "b351bb9b-0af6-34fc-a787-49675c53ad67",
  "name": "Madagascar",
  "description": "reference location, sources: ISO 3166-1, ecoinvent 3, ILCD, GaBi",
  "version": "00.00.000",
  "code": "MG",
  "latitude": -19.374,
  "longitude": 46.706
}
```

### `GET /locations/{id}/info`
* returns the descriptor of the location with the given ID
* response is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)


### `GET /units`
* returns the descriptors of the available units
* each descriptor is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
[
  {
    "@type": "Unit",
    "@id": "1c3a9695-398d-4b1f-b07e-a8715b610f70",
    "name": "m3",
    "description": "Cubic meter",
    "version": "00.00.000"
  }
]
```

### `GET /units/{id}`
* returns the full unit with the given ID
* response is of type [Unit](http://greendelta.github.io/olca-schema/Unit.html)
* example response:

```json
{
  "@type": "Unit",
  "@id": "010f811e-3cc2-4b14-a901-337da9b3e49c",
  "name": "kcal",
  "description": "Kilocalorie (International table)",
  "version": "00.00.000",
  "conversionFactor": 0.0041867
}
```

### `GET /units/{id}/info`
* returns the descriptor of the unit with the given ID
* response is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)

### `GET /unit-groups`
* returns the descriptors of the available unit groups
* each descriptor is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
[
  {
    "@type": "UnitGroup",
    "@id": "258d6abd-14f2-4484-956c-c88e8f6fd8ed",
    "name": "Units of energy/mass*time",
    "categoryPath": [
      "Technical unit groups"
    ],
    "version": "01.00.001"
  }
]
```

### `GET /unit-groups/{id}`
* returns the full unit group with the given ID
* response is of type [UnitGroup](http://greendelta.github.io/olca-schema/UnitGroup.html)

### `GET /unit-groups/{id}/info`
* returns the descriptor of the unit group with the given ID
* response is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
{
  "@type": "UnitGroup",
  "@id": "93a60a57-a4c8-11da-a746-0800200c9a66",
  "name": "Units of mass",
  "categoryPath": [
    "Technical unit groups"
  ],
  "version": "01.00.001"
}
```

### `GET /flow-properties`
* returns the descriptors of the available flow properties
* each descriptor is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
[
  {
    "@type": "FlowProperty",
    "@id": "93a60a56-a3c8-11da-a746-0800200b9a66",
    "name": "Mass",
    "categoryPath": [
      "Technical flow properties"
    ],
    "version": "01.00.001"
  }
]
```

### `GET /flow-properties/{id}`
* returns the full flow property with the given ID
* response is of type [FlowProperty](http://greendelta.github.io/olca-schema/FlowProperty.html)

### `GET /flow-properties/{id}/info`
* returns the descriptor of the flow property with the given ID
* response is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
{
  "@type": "FlowProperty",
  "@id": "93a60a56-a3c8-11da-a746-0800200b9a66",
  "name": "Mass",
  "categoryPath": [
    "Technical flow properties"
  ],
  "version": "01.00.001"
}
```

### `GET /flows`
* returns the descriptors of the available flows
* each descriptor is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
[
  {
    "@type": "Flow",
    "@id": "0017271e-7df5-40bc-833a-36110c1fe5d5",
    "name": "Nitrite",
    "categoryPath": [
      "Elementary flows",
      "Emission to water",
      "surface water"
    ],
    "flowType": "ELEMENTARY_FLOW",
    "refUnit": "kg",
    "version": "01.00.002"
  }
]
```

### `GET /flows/{id}`
* returns the full flow with the given ID
* response is of type [Flow](http://greendelta.github.io/olca-schema/Flow.html)

### `GET /flows/{id}/info`
* returns the descriptor of the flow with the given ID
* response is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
{
  "@type": "Flow",
  "@id": "3aeccfbd-5f70-480b-9474-f496f64371d0",
  "name": "Car, diesel-powered, disposed",
  "flowType": "PRODUCT_FLOW",
  "refUnit": "kg",
  "description": "",
  "version": "00.00.002"
}
```

### `GET /processes`
* returns the descriptors of the available processes
* each descriptor is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
[
  {
    "@type": "Process",
    "@id": "85c396b6-7e77-4dc8-89e7-d0db3a6a3dbb",
    "name": "Combustion, dry wood residue, AP-42",
    "categoryPath": [
      "Utilities",
      "Other Electric Power Generation"
    ],
    "processType": "UNIT_PROCESS",
    "location": "RNA",
    "version": "01.00.001"
  }
]
```

### `GET /processes/{id}`
* returns the full process with the given ID
* response is of type [Process](http://greendelta.github.io/olca-schema/Process.html)

### `GET /processes/{id}/info`
* returns the descriptor of the process with the given ID
* response is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
{
  "@type": "Process",
  "@id": "03ee9f75-713c-447b-b765-a0686db08a14",
  "name": "corn stover, ground and stored",
  "categoryPath": [
    "biomass",
    "production"
  ],
  "processType": "UNIT_PROCESS",
  "location": "RNA",
  "description": "Taken from Sheehan, Corn Stover Ethanol LCA ...",
  "version": "01.00.001"
}
```

### `POST /processes`
* Creates a new process.
* request is of type [Process](http://greendelta.github.io/olca-schema/Process.html)
* example request:

```json
{
  "name": "My custom process",
  "processType": "UNIT_PROCESS",
  "description": "My custom process description...",
  "version": "01.00.001",
  "exchanges": [
    {
      "@type": "Exchange",
      "avoidedProduct": false,
      "input": false,
      "amount": 1055.87,
      "flow": {
        "@type": "Flow",
        "@id": "014be78c-e624-4a1e-9193-972866fc55cd",
        "name": "Combustion, dry wood residue, AP-42",
        "categoryPath": [
          "Product flows"
        ],
        "flowType": "PRODUCT_FLOW",
        "location": "RNA",
        "refUnit": "MJ"
      },
      "unit": {
        "@type": "Unit",
        "@id": "52765a6c-3896-43c2-b2f4-c679acf13efe",
        "name": "MJ"
      },
      "flowProperty": {
        "@type": "FlowProperty",
        "@id": "f6811440-ee37-11de-8a39-0800200c9a66",
        "name": "Energy",
        "categoryPath": [
          "Technical flow properties"
        ]
      },
      "quantitativeReference": true
    },
    ...
  ]
}
```

* response is of type [Process](http://greendelta.github.io/olca-schema/Process.html)

### `PUT /processes/{id}`
* Updates an existing process.
* request is of type [Process](http://greendelta.github.io/olca-schema/Process.html)
* example request:

```json
{
  "name": "My new process name",
  "description": "My new process description...",
}
```

* response is of type [Process](http://greendelta.github.io/olca-schema/Process.html)

### `DELETE /processes/{id}`
* Deletes an existing process.
* request body is not needed.
* example response:

```json
{
  "message": "Process with ID=d8ef5198-0dc1-4259-a548-0a97f5dba074 deleted"
}
```

### `GET /models`
* returns the descriptors of the available models
* each descriptor is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
[
  {
    "@type":"ProductSystem",
    "@id":"ba73d06a-d055-41fd-9611-9cb7c7e0d646",
    "name":"Diesel car, use phase",
    "description":"First created: 2019-09-05T11:58:29 ...",
    "version":"00.00.005"
  }
]
```

### `GET /models/{id}`
* returns the full model with the given ID
* response is of type [ProductSystem](http://greendelta.github.io/olca-schema/ProductSystem.html)

### `GET /models/{id}/info`
* returns the descriptor of the model with the given ID
* response is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)
* example response:

```json
{
  "@type":"ProductSystem",
  "@id":"ba73d06a-d055-41fd-9611-9cb7c7e0d646",
  "name":"Diesel car, use phase",
  "description":"First created: 2019-09-05T11:58:29 ...",
  "version":"00.00.005"
}
```

### `GET /models/{id}/parameters`
* returns the parameters of the model with the given ID
* each parameter is of type [ParameterRedef](http://greendelta.github.io/olca-schema/ParameterRedef.html)
* example response:

```json
[
  {
    "name":"f_value",
    "value":0.9
  },
  {
    "name": "cons",
    "value": 0.08,
    "context": {
      "@type": "Process",
      "@id": "0b5125f7-d818-4b07-94ff-6b88ab044416",
      "name": "Diesel car, use phase",
      "categoryPath": [
        "Case study Diesel car"
      ],
      "processType": "UNIT_PROCESS",
      "description": "It is assumed that the lifetime mileage of the car is 211000 km",
      "version": "00.00.004"
    }
  }
]
```

### `GET /methods`
* returns the descriptors of the available LCIA methods
* each descriptor is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)

### `GET /methods/{id}`
* returns the full LCIA method with the given ID
* response is of type [ImpactMethod](http://greendelta.github.io/olca-schema/ImpactMethod.html)

### `GET /methods/{id}/indicators`
* returns the LCIA indicators of the LCIA method with the given ID
* each indicator is of type [Ref](http://greendelta.github.io/olca-schema/Ref.html)

### `POST /calculate`
* calculates the result for a given calculation setup
* request is of type [CalculationSetup](http://greendelta.github.io/olca-schema/CalculationSetup.html)
* example request with [ProductSystem](http://greendelta.github.io/olca-schema/ProductSystem.html):

```json
{
  "productSystem": {
    "@type": "ProductSystem",
    "@id": "ba73d06a-d055-41fd-9611-9cb7c7e0d646",
    "name": "Diesel car, use phase"
  },
  "impactMethod": {
    "@type": "ImpactMethod",
    "@id": "363634a5-ef5c-4ac7-9a32-f421106f4ecc",
    "name": "ILCD 2011, midpoint"
  },
  "amount": 1.0,
  "parameterRedefs": [
    {
      "name": "cons",
      "value": 0.08,
      "context": {
        "@type": "Process",
        "@id": "0b5125f7-d818-4b07-94ff-6b88ab044416",
        "name": "Diesel car, use phase"
      }
    },
    {
      "name": "f_value",
      "value": 0.9
    },
    {
      "name": "distance",
      "value": 211000.0,
      "context": {
        "@type": "Process",
        "@id": "0b5125f7-d818-4b07-94ff-6b88ab044416",
        "name": "Diesel car, use phase"
      }
    }
  ]
}
```
* example request with [Process](http://greendelta.github.io/olca-schema/Process.html):

```json
{
  "process": {
    "@type": "Process",
    "@id": "0b5125f7-d818-4b07-94ff-6b88ab044416",
    "name": "Diesel car, use phase"
  },
  "impactMethod": {
    "@type": "ImpactMethod",
    "@id": "363634a5-ef5c-4ac7-9a32-f421106f4ecc",
    "name": "ILCD 2011, midpoint"
  },
  "amount": 1.0,
  "parameterRedefs": [
    {
      "name": "cons",
      "value": 0.08,
      "context": {
        "@type": "Process",
        "@id": "0b5125f7-d818-4b07-94ff-6b88ab044416",
        "name": "Diesel car, use phase"
      }
    },
    {
      "name": "f_value",
      "value": 0.9
    },
    {
      "name": "distance",
      "value": 211000.0,
      "context": {
        "@type": "Process",
        "@id": "0b5125f7-d818-4b07-94ff-6b88ab044416",
        "name": "Diesel car, use phase"
      }
    }
  ]
}
```

* response is of type [SimpleResult](http://greendelta.github.io/olca-schema/SimpleResult.html)
* example response:

```json
{
  "flowResults": [
    {
      "@type": "FlowResult",
      "flow": {
        "@type": "Flow",
        "@id": "bd601c88-f05e-436a-b4ab-5b3d4fb29882",
        "name": "Americium-241",
        "categoryPath": [
          "Elementary flows",
          "Emission to water",
          "unspecified"
        ],
        "flowType": "ELEMENTARY_FLOW",
        "refUnit": "kBq"
      },
      "input": false,
      "value": 1.241467556012079e-09
    },
    {
      "@type": "FlowResult",
      "flow": {
        "@type": "Flow",
        "@id": "6a9ef0ac-2a82-492b-90f7-7f8d09995bbc",
        "name": "Benzene, pentachloro-",
        "categoryPath": [
          "Elementary flows",
          "Emission to air",
          "unspecified"
        ],
        "flowType": "ELEMENTARY_FLOW",
        "refUnit": "kg"
      },
      "input": false,
      "value": 6.609724987708954e-11
    }
  ],
  "impactResults": [
    {
      "@type": "ImpactResult",
      "impactCategory": {
        "@type": "ImpactCategory",
        "@id": "b79872d9-703f-4ccc-b02c-13f08d22c400",
        "name": "Freshwater eutrophication",
        "refUnit": "kg P eq"
      },
      "value": 0.00857420704954431
    },
    {
      "@type": "ImpactResult",
      "impactCategory": {
        "@type": "ImpactCategory",
        "@id": "95b7bf3a-2464-4857-9855-d2ee53f94436",
        "name": "Metal depletion",
        "refUnit": "kg Fe eq"
      },
      "value": 3654.9568395153387
    }
  ]
}
```

### `POST /calculate/tags`
* calculates the tagged results for a given calculation setup
* request is of type [CalculationSetup](http://greendelta.github.io/olca-schema/CalculationSetup.html). Analogous to `POST /calculate`
* example response:

```json
{
  "impactResults": [
    {
      "@type": "ImpactResult",
      "impactCategory": {
        "@type": "ImpactCategory",
        "@id": "5a93a323-f61e-37c0-986a-adacac408cda",
        "name": "Global warming (GWP100a)",
        "refUnit": "kg CO2 eq"
      },
      "value": 0.293517177203566
    },
    {
      "@type": "ImpactResult",
      "impactCategory": {
        "@type": "ImpactCategory",
        "@id": "945a6ca5-25a7-30de-9fd5-858c6381de3b",
        "name": "Water use",
        "refUnit": "m3 depriv."
      },
      "value": 0.09963534078272376
    }
  ],
"tagResults": [
  {
      "@type": "TagResult",
      "impactCategory": {
        "@type": "ImpactCategory",
        "@id": "5a93a323-f61e-37c0-986a-adacac408cda",
        "name": "Global warming (GWP100a)",
        "refUnit": "kg CO2 eq"
      },
      "tags": [
        {
          "tag": "material",
          "value": 0.0
        },
        {
          "tag": "waste treatment",
          "value": 3.526563149512787E-5
        },
        {
          "tag": "assembly",
          "value": 0.0
        },
        {
          "tag": "transport",
          "value": 2.502566124112862E-4
        },
        {
          "tag": "waste scenario",
          "value": 0.0
        }
      ]
    },
    {
      "@type": "TagResult",
      "impactCategory": {
        "@type": "ImpactCategory",
        "@id": "945a6ca5-25a7-30de-9fd5-858c6381de3b",
        "name": "Water use",
        "refUnit": "m3 depriv."
      },
      "tags": [
        {
          "tag": "material",
          "value": 0.0
        },
        {
          "tag": "waste treatment",
          "value": 2.064197003892192E-6
        },
        {
          "tag": "assembly",
          "value": 0.0
        },
        {
          "tag": "transport",
          "value": 9.597162775959336E-7
        },
        {
          "tag": "waste scenario",
          "value": 0.0
        }
      ]
    }
  ]
}
```

### The `client.py` example

The example script `client.py` demonstrates the usage of that API. It selects
a random model and LCIA method and runs a number of calculations where the
model parameters are slightly modified in each iteration. The result of one
indicator is reported on the standard output.

