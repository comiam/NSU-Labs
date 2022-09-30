package me.bolshim

import org.apache.commons.cli.*

private const val FILE_OPTION = "file"
private const val HELP_OPTION = "help"
private const val SHORT_FILE_OPTION = "f"
private const val SHORT_HELP_OPTION = "h"

private fun makeOptions(): Options {
    val readFileOption = Option.builder(SHORT_FILE_OPTION)
        .hasArg(true)
        .longOpt(FILE_OPTION)
        .desc(" path to the source file")
        .build()
    val helpOption = Option.builder()
        .option(SHORT_HELP_OPTION)
        .longOpt(HELP_OPTION)
        .desc("Print help message.")
        .build()
    val options = Options()
    options.addOption(readFileOption)
    options.addOption(helpOption)
    return options
}

class CliOptions(args: Array<String>) {
    private val cli: CommandLine
    private val options: Options = makeOptions()

    init {
        cli = DefaultParser().parse(options, args, true)
    }

    val help
        get() = cli.hasOption(HELP_OPTION)
    val fileName: String
        get() = cli.getOptionValue(FILE_OPTION)

    fun printHelp() =
        HelpFormatter().printHelp("Extract Open Street Map data and compute stats", options)

}