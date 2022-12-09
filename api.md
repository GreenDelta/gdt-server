
# openLCA Rest API

A Rest API for the openLCA service backend.

## Group: `/data`

The routes for the methods under this group often take a `type` parameter that
maps to a corresponding type of the
[openLCA schema](https://greendelta.github.io/olca-schema/). The table below
shows which values for this parameter map to which type of the openLCA schema.
Multiple parameter values map to the same time, e.g. often the singular and
plural form can be used.


| Parameter                                                     | openLCA schema type                                                                      |
|---------------------------------------------------------------|------------------------------------------------------------------------------------------|
| `actor `,  `actors `                                          | [Actor](https://greendelta.github.io/olca-schema/classes/Actor.html)                     |
| `category `,  `categories `                                   | [Category](https://greendelta.github.io/olca-schema/classes/Category.html)               |
| `currency `,  `currencies `                                   | [Currency](https://greendelta.github.io/olca-schema/classes/Currency.html)               |
| `dq-system `,  `dq-systems `                                  | [DQSystem](https://greendelta.github.io/olca-schema/classes/DQSystem.html)               |
| `epd `,  `epds `                                              | [Epd](https://greendelta.github.io/olca-schema/classes/Epd.html)                         |
| `flow `,  `flows `                                            | [Flow](https://greendelta.github.io/olca-schema/classes/Flow.html)                       |
| `flow-property `,  `flow-properties `                         | [FlowProperty](https://greendelta.github.io/olca-schema/classes/FlowProperty.html)       |
| `impact-category `,  `impact-categories `                     | [ImpactCategory](https://greendelta.github.io/olca-schema/classes/ImpactCategory.html)   |
| `impact-method `,  `method `,  `impact-methods `,  `methods ` | [ImpactMethod](https://greendelta.github.io/olca-schema/classes/ImpactMethod.html)       |
| `location `,  `locations `                                    | [Location](https://greendelta.github.io/olca-schema/classes/Location.html)               |
| `parameter `,  `parameters `                                  | [Parameter](https://greendelta.github.io/olca-schema/classes/Parameter.html)             |
| `process `,  `processes `                                     | [Process](https://greendelta.github.io/olca-schema/classes/Process.html)                 |
| `product-system `,  `model `,                                 | [ProductSystem](https://greendelta.github.io/olca-schema/classes/ProductSystem.html)     |
| `project `,  `projects `                                      | [Project](https://greendelta.github.io/olca-schema/classes/Project.html)                 |
| `result `,  `results `                                        | [Result](https://greendelta.github.io/olca-schema/classes/Result.html)                   |
| `social-indicator `,  `social-indicators `                    | [SocialIndicator](https://greendelta.github.io/olca-schema/classes/SocialIndicator.html) |
| `source `,  `sources `                                        | [Source](https://greendelta.github.io/olca-schema/classes/Source.html)                   |
| `unit-group `,  `unit-groups `                                | [UnitGroup](https://greendelta.github.io/olca-schema/classes/UnitGroup.html)             |


### `GET /data/{type}`

This method returns the descriptors of all data sets of the given type from the
database.

* Return type: [`List[Ref]`](https://greendelta.github.io/olca-schema/classes/Ref.html)


### `GET /data/{type}/{id}`

This method returns the full data set of the given type and ID.

* Return type: [`E : RootEntity`](https://greendelta.github.io/olca-schema/classes/RootEntity.html)


### `GET /data/{type}/{id}/info`



* returns the descriptor of the data set of the given type and ID

### `GET /data/{type}/{id}/parameters`
* returns the parameters of the specified data set, this is only
  valid for data set types that can have parameters or parameter
  redefinitions

### `PUT /data/{type}`
* inserts or updates the provided data set in the database; this method
  is not available when the server runs in read-only mode

### `DELETE /data/{type}/{id}`
* deletes the specified data set from the database; this method is not
  available when the server runs in read-only mode

### `POST /results/calculate`
* start a calculation for the provided setup, returns the calculation
  state with the result ID

### `DELETE | POST /results/{id}/dispose`
* disposes the result with the given ID

### `GET /results/{id}/state`
* returns the calculation state of the result with the given ID

### `GET  /results/{id}/total-impacts`
* returns the total LCIA result
