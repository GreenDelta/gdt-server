import copy
import math
import requests
import random
from typing import List, Optional


ENDPOINT = 'http://192.168.142.128:8080'
ITERATIONS = 100


def main():

    # fetch and select a model
    models: List[dict] = requests.get(ENDPOINT + '/models').json()
    if len(models) == 0:
        print('ERROR: no model could be found at')
        return
    model = models[0]
    model_id = model.get('@id')
    print(f"Selected model: {model.get('name')}; id={model_id}")

    # fetch the model parameters
    params: List[dict] = requests.get(
        f'{ENDPOINT}/models/{model_id}/parameters').json()
    if len(params) == 0:
        print('  has no public parameters')
    else:
        print(f'  has {len(params)} public parameter(s):')
        for param in params:
            print(f"    * {param.get('name')}; "
                  f"default value = {param.get('value')}")

    # fetch and select an LCIA method
    methods: List[dict] = requests.get(ENDPOINT + '/methods').json()
    if len(methods) == 0:
        print('ERROR: no LCIA method could be found')
        return
    method = random.choice(methods)
    method_id = method.get('@id')
    print(f"Selected LCIA method: {method.get('name')}; id={method_id}")

    # fetch and select an LCIA indicator
    indicators: List[dict] = requests.get(
        f'{ENDPOINT}/methods/{method_id}/indicators').json()
    if len(indicators) == 0:
        print("ERROR: method has no LCIA categories")
        return
    print(f'  has {len(indicators)} indicators')
    indicator = random.choice(indicators)
    indicator_id = indicator.get('@id')
    print(f"  selected {indicator.get('name')} [{indicator.get('refUnit')}]; "
          f"id={indicator_id}")

    print('\nRun calculations:')
    # prepare the calculation setup
    setup = {
        'productSystem': model,
        'impactMethod': method,
        'amount': 1.0,
        'parameterRedefs': params,
    }
    for i in range(0, ITERATIONS):
        factor = math.sin(i * math.pi / 8) * 0.5
        factor += factor * 0.1 * random.random()
        next_params = []
        for param in params:
            next_param = copy.deepcopy(param)
            value: float = next_param['value']
            next_param['value'] = value + factor * value
            next_params.append(next_param)
        setup['parameterRedefs'] = next_params
        result: dict = requests.post(
            ENDPOINT + '/calculate', json=setup).json()
        value = find_result(indicator_id, result)
        print(80 * ' ', end='\r')
        print(f' iteration={i+1}; df={factor}, result={value}', end='\r')
    print('\ndone')


def find_result(indicator_id: str, result: dict) -> Optional[float]:
    if not result:
        return None
    impacts: List[dict] = result.get('impactResults')
    if not impacts:
        return None
    for impact in impacts:
        ic: dict = impact.get('impactCategory')
        if not ic:
            continue
        if ic.get('@id') == indicator_id:
            return impact.get('value', 0.0)
    return None


if __name__ == '__main__':
    main()
