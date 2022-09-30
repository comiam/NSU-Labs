from math import log
from typing import List, Tuple

from prettytable import PrettyTable
import pandas as pd
import numpy as np
import seaborn as sns
from matplotlib import pyplot as plt

from ReliefF import ReliefF

def delete_nan_cols(df):
    # series with column's nan count
    nan_count_series = df.isna().sum()
    column_size = df.shape[0]

    for i, v in nan_count_series.iteritems():
        if (v / column_size) * 100 > 45 and i != "G_total":  # тут меняли с 60 на 45
            df.drop(i, axis=1, inplace=True)

    return df


def clear_column_from_extremes(data_frame: pd.DataFrame, col_name: str):
    column = data_frame[col_name]
    q = np.nanquantile(column, q=[0.25, 0.75])
    low = q[0] - 1.5 * (q[1] - q[0])
    high = q[1] + 1.5 * (q[1] - q[0])

    for index, row in data_frame.iterrows():
        aux = data_frame[col_name].loc[index]
        if not (low <= aux < high) and np.isnan(data_frame["G_total"].loc[index]):
            data_frame.drop(index, inplace=True)

    return data_frame


def delete_extreme_vals(df):
    for i in ['Рлин', 'Рлин_1', 'Туст', 'Тна шлейфе', 'Тзаб', 'Дебит ст. конд.', 'Дебит кон нестабильный',
              'Рпл. Тек (Карноухов)', 'Удельная плотность газа ']:
        clear_column_from_extremes(df, i)
        # vals1 = df[i].to_numpy()
        # sns.displot(vals1, kde=True, bins=int(1+log(len(vals1), 2)))
        # plt.title(f'{i}')
        # plt.savefig(i + '.jpg')

    return df


def delete_small_uniques(df):
    del_list = []
    for i in list(df):
        if df[i].nunique(dropna=True) == 1:
            del_list.append(i)

    for i in del_list:
        df.drop(i, axis=1, inplace=True)

    return df


def parse_xlsx() -> pd.DataFrame:
    df = pd.read_csv('data/clean.csv')

    return df


def clear_dataframe(df):
    # delete empty samples with empty classes
    df.dropna(subset=["КГФ", "G_total"], how='all', inplace=True)

    df = delete_extreme_vals(df)
    df = delete_nan_cols(df)
    df = delete_small_uniques(df)

    return df


def find_interval(value: float, kgf_intervals: List[Tuple[float, float]]):
    for i in kgf_intervals:
        if i[0] <= value < i[1]:
            return i
    return np.NaN


def get_class_list(df):
    kgf = sorted(df[df.columns[-1]].unique())

    bins = 1 + log(len(kgf), 2)
    min = kgf[0]
    max = kgf[len(kgf) - 1]
    length = (max - min) / bins

    kgf_int = [(min + length * i, min + length * (i + 1)) for i in range(int(bins))]

    classes = set()

    for index, row in df.iterrows():
        g_val = df["G_total"].loc[index]
        kgf_val = df["КГФ"].loc[index]

        contains = False
        for i in classes:
            if find_interval(kgf_val, kgf_int) == i[1] and (np.isnan(i[0]) and np.isnan(g_val) or i[0] == g_val):
                contains = True
                break
        if not contains:
            classes.add((g_val, find_interval(kgf_val, kgf_int)))

    return classes, kgf_int


def freq(intervals, classes, df):
    fq = {clT: 0 for clT in classes}

    for index, row in df.iterrows():
        g_val = df["G_total"].loc[index]
        kgf_val = df["КГФ"].loc[index]

        for i in classes:
            interval = find_interval(kgf_val, intervals)
            if interval == i[1] or np.all(np.isnan(i[1])) and np.all(np.isnan(interval)):
                if np.isnan(i[0]) and np.isnan(g_val) or i[0] == g_val:
                    fq[i] = fq[i] + 1
                    break

    return fq


def info(intervals, classes, df):
    infoT = 0.0
    count = df.shape[0]

    for i in freq(intervals, classes, df).values():
        if i != 0:
            infoT += i / count * np.log2(i / count)

    return -infoT


def gain_ratio(intervals, classes, feature_name, df):
    infoT = info(intervals, classes, df)
    infoTX = 0.0
    splitX = 0.0

    for i in df[feature_name].unique():
        # if np.isnan(i):
        #    continue
        dfI = df[np.isnan(df[feature_name])] if np.isnan(i) else df[df[feature_name] == i]

        infoTX += dfI.shape[0] / df.shape[0] * info(intervals, classes, dfI)
        splitX += dfI.shape[0] / df.shape[0] * np.log2(dfI.shape[0] / df.shape[0])

    splitX *= -1

    return (infoT - infoTX) / splitX


def get_high_correlated(so):
    saved = []
    for i in so.items():
        if "КГФ" in i[0] or "G_total" in i[0]:
            continue
        if abs(i[1]) >= 0.75:
            saved.append(i[0])

    return saved


def find_problematic_pairs(highly_correlated, corr_mat):
    attributes = corr_mat.columns.values

    leave_only_one = []

    for h1, h2 in highly_correlated:
        need_both = False
        for other in attributes:
            if other == h1 or other == h2:
                continue

            corr_h1_other = corr_mat[other][h1] if np.isnan(corr_mat[h1][other]) else corr_mat[h1][other]
            corr_h2_other = corr_mat[h2][other] if np.isnan(corr_mat[other][h2]) else corr_mat[other][h2]

            if np.isnan(corr_h2_other) or np.isnan(corr_h1_other):
                continue

            if abs(corr_h2_other - corr_h1_other) >= 0.3:
                need_both = True
                break

        if not need_both:
            leave_only_one.append((h1, h2))

    return leave_only_one


def find_correlations_and_compare(data_frame, gains):
    cor = data_frame.corr()

    plt.clf()
    sns.heatmap(cor, xticklabels=range(data_frame.shape[1]), yticklabels=1, linewidths=.5)
    plt.savefig("Heatmap.jpg")

    cor.values[np.tril_indices(len(cor))] = np.NaN
    so = cor.unstack().sort_values(kind="quicksort")
    so = so[~np.isnan(so)]

    saved_final = set()
    deleted = set()

    for i, j in find_problematic_pairs(get_high_correlated(so), cor):
        if gains[i] > gains[j]:
            if i not in deleted:
                saved_final.add((i, round(gains[i], 5)))
                deleted.add(j)
            else:
                saved_final.add((j, round(gains[j], 5)))
        else:
            if j not in deleted:
                saved_final.add((j, round(gains[j], 5)))
                deleted.add(i)
            else:
                saved_final.add((i, round(gains[i], 5)))

    return saved_final


def delete_nan_vals(df):
    sred = {i: df[i].mean() for i in df}

    for index, row in df.iterrows():
        for col in df:
            if np.isnan(df[col].loc[index]):
                df[col].loc[index] = sred[col]

    return df


def testReliefF(df: pd.DataFrame, intervals, classes):
    lc = list(classes)
    classID = {i: lc.index(i) for i in classes}

    for index, row in df.iterrows():
        g_val = df["G_total"].loc[index]
        kgf_val = df["КГФ"].loc[index]

        for i in classes:
            interval = find_interval(kgf_val, intervals)
            if interval == i[1] or np.all(np.isnan(i[1])) and np.all(np.isnan(interval)):
                if np.isnan(i[0]) and np.isnan(g_val) or i[0] == g_val:
                    df["G_total"].loc[index] = classID[i]
                    break

    del df["КГФ"]

    data = delete_nan_vals(df.drop("G_total", 1)).to_numpy()
    classes = df["G_total"].to_numpy()

    fs = ReliefF(n_neighbors=20, n_features_to_keep=11)
    X_train = fs.fit_transform(data, classes)
    print(X_train)

    # ДЕбит ст конд
    # Рзаб
    # Тлин
    # Дебит газа
    # Руст
    # Дебит смеси
    # Дебит кон нестабильный
    # Рсб
    # Зсб_1
    # Рлин
    # Тзаб

    # совпал по Gain ration по 7 признакам


def main() -> None:
    data = parse_xlsx()
    data = clear_dataframe(data)
    setCI = get_class_list(data)

    testReliefF(data.copy(), setCI[1], setCI[0])

    gains = {i: gain_ratio(setCI[1], setCI[0], i, data) for i in data}
    gains = dict(sorted(gains.items(), key=lambda item: item[1]))
    del gains['G_total']
    del gains['КГФ']

    plt.barh(list(gains.keys()), width=list(gains.values()))
    plt.savefig('Gains.jpg')

    t = PrettyTable(['Attribute name', 'Gain Ratio'])

    for i in sorted(find_correlations_and_compare(data, gains), key=lambda x: x[1], reverse=True):
        t.add_row([i[0], i[1]])

    print(t)


if __name__ == '__main__':
    main()
