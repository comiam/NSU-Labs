import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import requests


def plot_correlation_matrix(df, graph_width):
    df = df.dropna('columns')  # drop columns with NaN
    df = df[[col for col in df if df[col].nunique() > 1]]  # keep columns where there are more than 1 unique values
    if df.shape[1] < 2:
        print(f'No correlation plots shown: The number of non-NaN or constant columns ({df.shape[1]}) is less than 2')
        return
    corr = df.corr()
    plt.figure(num=None, figsize=(graph_width, graph_width), dpi=80, facecolor='w', edgecolor='k')
    corr_mat = plt.matshow(corr, fignum=1)
    plt.xticks(range(len(corr.columns)), corr.columns, rotation=90)
    plt.yticks(range(len(corr.columns)), corr.columns)
    plt.gca().xaxis.tick_bottom()
    plt.colorbar(corr_mat)
    plt.title(f'Корреляционная матрица', fontsize=15)
    plt.show()


def plot_scatter_matrix(df: pd.DataFrame, plot_size: int, text_size: int) -> None:
    df = df.copy()
    df = df.select_dtypes(include=[np.number])
    df = df.dropna()
    df = df[[col for col in df if df[col].nunique() > 1]]
    column_names = list(df)
    if len(column_names) > 10:
        column_names = column_names[:10]
    df = df[column_names]
    ax = pd.plotting.scatter_matrix(df, alpha=0.75, figsize=(plot_size, plot_size), diagonal='kde')
    corrs = df.corr().values
    for i, j in zip(*plt.np.triu_indices_from(ax, k=1)):
        ax[i, j].annotate('Corr. coef = %.3f' % corrs[i, j], (0.8, 0.2), xycoords='axes fraction', ha='center',
                          va='center', size=text_size)
    plt.suptitle('График рассеяния и плотности')
    plt.show()


def ip_country(addr: str) -> str:
    url = f"https://geolocation-db.com/json/{addr}&position=true"
    res = requests.get(url)
    json_resp = res.json()
    return json_resp['country_name']
