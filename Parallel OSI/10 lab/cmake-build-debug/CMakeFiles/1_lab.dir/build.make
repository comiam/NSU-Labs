# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.17

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Disable VCS-based implicit rules.
% : %,v


# Disable VCS-based implicit rules.
% : RCS/%


# Disable VCS-based implicit rules.
% : RCS/%,v


# Disable VCS-based implicit rules.
% : SCCS/s.%


# Disable VCS-based implicit rules.
% : s.%


.SUFFIXES: .hpux_make_needs_suffix_list


# Command-line flag to silence nested $(MAKE).
$(VERBOSE)MAKESILENT = -s

# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /home/maxim/Programs/clion-2020.2.1/bin/cmake/linux/bin/cmake

# The command to remove a file.
RM = /home/maxim/Programs/clion-2020.2.1/bin/cmake/linux/bin/cmake -E rm -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab"

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/cmake-build-debug"

# Include any dependencies generated for this target.
include CMakeFiles/1_lab.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/1_lab.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/1_lab.dir/flags.make

CMakeFiles/1_lab.dir/main.c.o: CMakeFiles/1_lab.dir/flags.make
CMakeFiles/1_lab.dir/main.c.o: ../main.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/cmake-build-debug/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_1) "Building C object CMakeFiles/1_lab.dir/main.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/1_lab.dir/main.c.o   -c "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/main.c"

CMakeFiles/1_lab.dir/main.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/1_lab.dir/main.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/main.c" > CMakeFiles/1_lab.dir/main.c.i

CMakeFiles/1_lab.dir/main.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/1_lab.dir/main.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/main.c" -o CMakeFiles/1_lab.dir/main.c.s

# Object files for target 1_lab
1_lab_OBJECTS = \
"CMakeFiles/1_lab.dir/main.c.o"

# External object files for target 1_lab
1_lab_EXTERNAL_OBJECTS =

1_lab: CMakeFiles/1_lab.dir/main.c.o
1_lab: CMakeFiles/1_lab.dir/build.make
1_lab: CMakeFiles/1_lab.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir="/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/cmake-build-debug/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_2) "Linking C executable 1_lab"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/1_lab.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/1_lab.dir/build: 1_lab

.PHONY : CMakeFiles/1_lab.dir/build

CMakeFiles/1_lab.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/1_lab.dir/cmake_clean.cmake
.PHONY : CMakeFiles/1_lab.dir/clean

CMakeFiles/1_lab.dir/depend:
	cd "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/cmake-build-debug" && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab" "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab" "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/cmake-build-debug" "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/cmake-build-debug" "/home/maxim/git/NSU-Labs/Parallel OSI/10 lab/cmake-build-debug/CMakeFiles/1_lab.dir/DependInfo.cmake" --color=$(COLOR)
.PHONY : CMakeFiles/1_lab.dir/depend

