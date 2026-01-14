package me.billbai.compiler.kwacc

import javax.xml.transform.Source

data class SourceFileInfo(
    val filePath: String,
    val preprocessedFilePath: String,
)

class SourceLocationInfo (
    val sourceFile: SourceFileInfo,
    val line: Int,
    val column: Int,
)