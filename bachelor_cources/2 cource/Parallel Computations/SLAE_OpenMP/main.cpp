#include <cstdlib>
#include <string>
#include <sstream>

namespace patch
{
    template <typename T> 
    std::string to_string(const T& n)
    {
        std::ostringstream stm;
        stm << n;
        return stm.str();
    }
}

#include <iostream>
#include "stdio.h"
#include "math.h"
#include "omp.h"

using namespace std;

double *getSolution(const double *matrix, const double *right, long size);
double *generateMatrix(double **right, long size);
double *fillMatrix(double **right, const string& fileName, const string& ansName);

int main(int argc, char *argv[])
{
    if (argc == 1)
    {
        cout << "No arg!" << endl;
        return 0;
    }
    if(argc > 5)
    {
        for (int i = 0; i < argc; ++i)
        {
            cout << argv[i] << endl;
        }
        cout << "Too many args!" << endl;
        return 0;
    }

    if(argc != 5 && argc != 3)
    {
        cout << "Invalid arg count!" << endl;
        return 0;
    }

    double *matrix = NULL;
    double *right = NULL;

    try
    {
        if(argc == 5)
            matrix = fillMatrix(&right, (string)argv[1], (string)argv[2]);
        else
            matrix = generateMatrix(&right, atoi(argv[1]));
    } catch (exception &e)
    {
        cout << "lol.. " << e.what() << endl;
        return 0;
    }

    double start_time = omp_get_wtime();
    if(argc == 5) //TODO FILE Tester Section
    {
        double *sol = getSolution(matrix, right, 2500);//TODO 2500 - size of matrix
        if(sol)
        {
            double end_time = omp_get_wtime();
            FILE* ifsAns = fopen(argv[3], "r");
            FILE* ans    = fopen(argv[4], "w");
            float a = 0;

            for (int i = 0; i < 2500; ++i)
            {
                fread(((void*)&a), sizeof(float), 1, ifsAns);
                fwrite(((void*)&a), sizeof(float), 1, ans);
            }
            cout << end_time - start_time << endl << "--------" << endl;
            fclose(ifsAns);
            fclose(ans);
            free(sol);
        } else
            cout << "Haven't solution!!!" << endl;
    } else
    {
        double *sol = getSolution(matrix, right, atoi(argv[1]));
        FILE* ans    = fopen(argv[4], "w");

        if(sol)
        {
            double end_time = omp_get_wtime();
            string a;
            for (int i = 0; i < atoi(argv[1]); ++i)
            {
                a = "x" + patch::to_string(i) + " = " + patch::to_string(sol[i]) + "\n";
                fwrite(((void*)a.c_str()), sizeof(char), a.length(), ans);
            }
            cout << end_time - start_time << endl << "--------" << endl;
            free(sol);
            fclose(ans);
        } else
            cout << "Haven't solution!!!" << endl;
    }

    free(matrix);
    free(right);
    return 0;
}

double *fillMatrix(double **right, const string& fileName, const string& ansName)
{
    double* matrix = new double[6250000];
    (*right)  = new double[2500];

    FILE* ifsMatrix = fopen(fileName.c_str(), "r");
    FILE* ifsAns= fopen(ansName.c_str(), "r");
    float a = 0;

    for (int i = 0; i < 6250000; ++i)
    {
        fread(((void*)&a), sizeof(float), 1, ifsMatrix);
        matrix[i] = a;
    }

    for (int i = 0; i < 2500; ++i)
    {
        fread(((void*)&a), sizeof(float), 1, ifsAns);
        (*right)[i] = a;
    }

    fclose(ifsMatrix);
    fclose(ifsAns);

    return matrix;
}

double *generateMatrix(double **right, long size)
{
    double* matrix = new double[size * size];
    (*right) = new double[size];

    for (int i = 0; i < size; i++)
    {
        (*right)[i] = (((double) rand() / (RAND_MAX)) + 1) * 3.0 * size - 1.0;
        for (int j = 0; j < size; j++)
            if (i == j)
                matrix[i * size + i] = (((double) rand() / (RAND_MAX)) + 1) * 2.0 * size;
            else
                matrix[i * size + j] = (((double) rand() / (RAND_MAX)) + 1) * 1.0;
    }

    return matrix;
}

double *getSolution(const double *matrix, const double *right, long size)
{
    double*  solution = new double[size];
    double*  solutionBuffer = new double[size];

    double tau = 0.015;
    bool diverge0 = false;
    bool diverge1 = false;
    bool flag = true;
    bool setOld = false;
    int divergeCount = 0;
    double oldValue = 0;

    double norm0 = 0, norm1 = 0;

    #pragma omp parallel shared(norm0, norm1, tau, size, right, divergeCount, oldValue, matrix, diverge0, diverge1, setOld, flag, solution, solutionBuffer, cout) default(none)
    {
        #pragma omp for reduction(+:norm0)
            for (int i = 0; i < size; ++i)
                norm0 += right[i] * right[i];

        #pragma omp single
        {
            norm0 = sqrt(norm0);
        }

        while (flag)
        {
            #pragma omp for reduction(+:norm1)
                for (int i = 0; i < size; ++i)
                {
                    double valueX = 0;

                    const double *m = matrix + i * size;
                    for (int j = 0; j < size; ++j) //Ax
                        valueX += m[j] * solution[j];

                    valueX -= right[i]; //Ax - b
                    solutionBuffer[i] = solution[i] - valueX * tau;
                    norm1 += valueX * valueX;
                }
            #pragma omp single
            {
                swap(solution, solutionBuffer);

                norm1 = sqrt(norm1);
                flag = (norm1 / norm0) > 1e-6;

                if (!setOld)
                    setOld = true;
                else if (oldValue < norm1 / norm0)
                    divergeCount++;

                //cout << norm1 / norm0 << endl;

                oldValue = norm1 / norm0;
                norm1 = 0;

                if (divergeCount > 50)
                {
                    if (diverge0)
                    {
                        diverge1 = true;
                        flag = false;
                    } else
                    {
                        diverge0 = true;
                        tau *= -1;
                        divergeCount = 0;
                    }
                }
            }
        }
    }

    free(solutionBuffer);

    if (diverge0 && diverge1)
    {
        free(solution);
        return NULL;
    } else
        return solution;
}