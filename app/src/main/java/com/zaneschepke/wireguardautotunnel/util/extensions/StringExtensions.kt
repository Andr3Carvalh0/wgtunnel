package com.zaneschepke.wireguardautotunnel.util.extensions

import timber.log.Timber
import java.util.regex.Pattern

internal val hasNumberInParentheses = """^(.+?)\((\d+)\)$""".toRegex()

internal fun String.isValidIpv4orIpv6Address(): Boolean {
	val ipv4Pattern = Pattern.compile(
		"^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\$",
	)
	val ipv6Pattern = Pattern.compile(
		"^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}\$",
	)
	return ipv4Pattern.matcher(this).matches() || ipv6Pattern.matcher(this).matches()
}

internal fun String.hasNumberInParentheses(): Boolean = hasNumberInParentheses.matches(this)

// Function to extract name and number
internal fun String.extractNameAndNumber(): Pair<String, Int>? {
	val matchResult = hasNumberInParentheses.matchEntire(this)
	return matchResult?.let {
		Pair(it.groupValues[1], it.groupValues[2].toInt())
	}
}

internal fun List<String>.isMatchingToWildcardList(value: String): Boolean {
	val excludeValues = this.filter { it.startsWith("!") }.map { it.removePrefix("!").transformWildcardsToRegex() }
	Timber.d("Excluded values: $excludeValues")
	val includedValues = this.filter { !it.startsWith("!") }.map { it.transformWildcardsToRegex() }
	Timber.d("Included values: $includedValues")
	val matches = includedValues.filter { it.matches(value) }
	val excludedMatches = excludeValues.filter { it.matches(value) }
	Timber.d("Excluded matches: $excludedMatches")
	Timber.d("Matches: $matches")
	return matches.isNotEmpty() && excludedMatches.isEmpty()
}

internal fun String.transformWildcardsToRegex(): Regex = this.replaceUnescapedChar("*", ".*").replaceUnescapedChar("?", ".").toRegex()

internal fun String.replaceUnescapedChar(charToReplace: String, replacement: String): String {
	val escapedChar = Regex.escape(charToReplace)
	val regex = "(?<!\\\\)(?<!(?<!\\\\)\\\\)($escapedChar)".toRegex()
	return regex.replace(this) { matchResult ->
		if (matchResult.range.first == 0 ||
			this[matchResult.range.first - 1] != '\\' ||
			(matchResult.range.first > 1 && this[matchResult.range.first - 2] == '\\')
		) {
			replacement
		} else {
			matchResult.value
		}
	}
}

internal fun Iterable<String>.joinAndTrim(): String = this.joinToString(", ").trim()

internal fun String.toTrimmedList(): List<String> = this.split(",").map { it.trim() }.filter { it.isNotEmpty() }
