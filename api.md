
# openLCA Rest API

A Rest API for the openLCA service backend.

## Group: `/data`

The routes for the methods under this group often take a `type` parameter that
maps to a corresponding type of the
[openLCA schema](https://greendelta.github.io/olca-schema/). The table below
shows which values for this parameter map to which type of the openLCA schema.
Multiple parameter values can map to the same type, e.g. often the singular and
plural form can be used.


| `type` value                                                  | openLCA schema type                                                                      |
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

Returns the descriptors of all data sets of the given type from the database.

* Return type: [`List[Ref]`](https://greendelta.github.io/olca-schema/classes/Ref.html)


### `GET /data/{type}/{id}`

Returns the full data set for the given type and ID.

* Return type: [`E : RootEntity`](https://greendelta.github.io/olca-schema/classes/RootEntity.html)


### `GET /data/{type}/name/{name}`

Returns the full data set for the given type and name. Note that the name does
not have to be unique in an openLCA database, and in this case, it will just
return the first entity from the database with the given name.

* Return type: [`E : RootEntity`](https://greendelta.github.io/olca-schema/classes/RootEntity.html)


### `GET /data/{type}/{id}/info`

Returns the descriptor of the data set with the given type and ID.

* Return type: [`Ref`](https://greendelta.github.io/olca-schema/classes/Ref.html)


### `GET /data/{type}/{id}/parameters`

Returns the (local) parameters of the specified data set. In case of processes
and impact categories, a list of parameters is returned. For product systems,
the respective parameter redefinitions are returned.

* Return type:
  * [`List[Parameter]`](https://greendelta.github.io/olca-schema/classes/Parameter.html)
    for processes and impact categories
  * [`List[ParameterRedef]`](https://greendelta.github.io/olca-schema/classes/ParameterRedef.html)
    for product systems


### `GET /data/providers`

Returns the product and waste treatment providers from the database. A provider
is a pair of process and product or process and waste flow for which results
can be calculated or which can be linked in product systems.

* Return type: [`List[TechFlow]`](https://greendelta.github.io/olca-schema/classes/TechFlow.html)


### `GET /data/providers/{flow-id}`

Returns the providers (see above) for a product or waste flow with the given ID.

* Return type: [`List[TechFlow]`](https://greendelta.github.io/olca-schema/classes/TechFlow.html)


### `PUT /data/{type}`

Inserts or updates the provided data set in the database; this method is not
available when the server runs in read-only mode.

* Request body: [`E : RootEntity`](https://greendelta.github.io/olca-schema/classes/RootEntity.html)
* Return type: [`Ref`](https://greendelta.github.io/olca-schema/classes/Ref.html)


### `DELETE /data/{type}/{id}`

Deletes the specified data set from the database; this method is not available
when the server runs in read-only mode.

* Return type: [`Ref`](https://greendelta.github.io/olca-schema/classes/Ref.html)


## Group `/results`


### `POST /results/calculate`

Schedules a new calculation for the provided calculation setup.

* Request body: [`CalculationSetup`](https://greendelta.github.io/olca-schema/classes/CalculationSetup.html)
* Return type: [`ResultState`](https://greendelta.github.io/olca-schema/classes/ResultState.html)


### `GET /results/{id}/state`

Returns the state of a result with the given ID.

* Return type: [`ResultState`](https://greendelta.github.io/olca-schema/classes/ResultState.html)


### `DELETE /results/{id} | POST /results/{id}/dispose`

Disposes the result with the given ID. It is important to call this method
when a result is not needed anymore to free resources.


### `GET  /results/{id}/total-impacts`

Returns the total impact assessment result.

* Return type: [`List[ImpactValue]`](https://greendelta.github.io/olca-schema/classes/ImpactValue.html)
