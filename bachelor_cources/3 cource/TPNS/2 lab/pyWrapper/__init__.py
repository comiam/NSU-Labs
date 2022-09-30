import subprocess

import plotly.graph_objs as go


def read_data_from_file(filepath: str) -> list:
    data_list = []
    with open(filepath, "r") as f:
        for line in f:
            data_list.append(float(line))
    return data_list


def exec_low_level_slave(path: str):
    proc = subprocess.Popen([path])
    proc.wait()


def draw_graph(x_list: list, y_list: list, name: str, x_axis_name: str, y_axis_name: str, color: str):
    fig = go.Figure()
    fig.add_trace(go.Scatter(x=x_list, y=y_list, name=name,
                             line=dict(color=color)))
    fig.update_traces(showlegend=True)
    fig.update_layout(legend_orientation="h", xaxis_title=x_axis_name, yaxis_title=y_axis_name)
    fig.show()


def main():
    # exec_low_level_slave("../cmake-build-release/perceptron_cpp.exe")
    average_error_list = read_data_from_file("trainMse.txt")
    average_error_list_len = len(average_error_list)
    kgf_error_list = read_data_from_file("kgf.txt")
    kgf_error_len = len(kgf_error_list)
    g_total_error_list = read_data_from_file("G_dev.txt")
    g_total_error_list_len = len(g_total_error_list)
    test_error = read_data_from_file("testMse.txt")
    len_test_error = len(test_error)
    draw_graph(x_list=[i for i in range(1, average_error_list_len)], y_list=average_error_list, name="avg_mse",
               x_axis_name="epoch", y_axis_name="avg_mse", color="red")
    draw_graph(x_list=[i for i in range(1, kgf_error_len)], y_list=kgf_error_list, name="кгф отклонение",
               x_axis_name="№", y_axis_name="отклонение", color="green")
    draw_graph(x_list=[i for i in range(1, g_total_error_list_len)], y_list=g_total_error_list, name="G_total_dev",
               y_axis_name="отклонение", x_axis_name="№", color="green")
    draw_graph(x_list=[i for i in range(1, len_test_error)], y_list=test_error, name="Test_mse",
               x_axis_name="epoch", y_axis_name="Test_mse", color="red")


if __name__ == "__main__":
    main()
