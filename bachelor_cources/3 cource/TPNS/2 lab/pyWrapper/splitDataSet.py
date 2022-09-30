import pandas as pd
from sklearn.model_selection import train_test_split

def split_stratified_into_train_val_test(frac_train=0.6,
                                         random_state=None):

    df_input = pd.read_csv('fullSet.csv')

    X = df_input # Contains all columns.
    y = df_input[df_input.columns[-1]] # Dataframe of just the column on which to stratify.

    # Split original dataframe into train and temp dataframes.
    df_train, df_temp, y_train, y_temp = train_test_split(X,
                                                          y,
                                                          stratify=y,
                                                          test_size=(1.0 - frac_train),
                                                          random_state=random_state)

    df_train.to_csv('newTrainSet.csv', sep=',', na_rep='nan')
    df_temp.to_csv('newTestSet.csv', sep=',', na_rep='nan')
    return df_train, df_temp


if __name__ == "__main__":
    split_stratified_into_train_val_test()
