class ZebraPuzzle() {

    enum class Color { RED, GREEN, YELLOW, IVORY, BLUE }
    enum class Nat { ENGLISH, SPANISH, UKRAIN, NORWEGIAN, JAPANESE }
    enum class Pet { DOGGO, SNAILS, FOX, HORSE, ZEBRA }
    enum class Drink { COVFEFE, TEA, MILK, ORANGE_JUICE, WATER }
    enum class Cigarette { OLD_GOLD, KOOLS, CHESTERFIELD, LUCKY_STRIKE, PARLAMENT }

    data class House(
        val position: Int,
        val color: Color,
        val nationality: Nat,
        val pet: Pet,
        val bevarage: Drink,
        val smoking: Cigarette,
    )

    interface Validation {
        fun isValid(solution: List<House>): Boolean
    }

    data class Rule(
        val condition1: (House) -> Boolean,
        val condition2: (House) -> Boolean
    ) : Validation {

        override fun isValid(houses: List<House>): Boolean {
            return houses.all { house -> condition1(house) == condition2(house) }
        }
    }

    class PositionRule(
        val sourceCondition: (House) -> Boolean,
        val relativePositionGetter: (Int) -> List<Int>,
        val targetCondition: (House) -> Boolean
    ) : Validation {

        override fun isValid(houses: List<House>): Boolean {
            return houses.filter { h -> sourceCondition(h) }
                .all { h ->
                    relativePositionGetter(h.position)
                        .filter { i -> i in 1..5 }
                        .any { i ->
                            houses.filter { h -> h.position == i }.all { h -> targetCondition(h) }
                        }
                }
        }
    }

    val rules: List<Validation> = listOf(
        Rule({ h -> h.nationality == Nat.ENGLISH }, { h -> h.color == Color.RED }),
        Rule({ h -> h.nationality == Nat.SPANISH }, { h -> h.pet == Pet.DOGGO }),
        Rule({ h -> h.bevarage == Drink.COVFEFE }, { h -> h.color == Color.GREEN }),
        Rule({ h -> h.nationality == Nat.UKRAIN }, { h -> h.bevarage == Drink.TEA }),
        PositionRule({ h -> h.color == Color.IVORY }, { i -> listOf(i + 1) }, { h -> h.color == Color.GREEN }),
        Rule({ h -> h.smoking == Cigarette.OLD_GOLD }, { h -> h.pet == Pet.SNAILS }),
        Rule({ h -> h.smoking == Cigarette.KOOLS }, { h -> h.color == Color.YELLOW }),
        Rule({ h -> h.position == 3 }, { h -> h.bevarage == Drink.MILK }),
        Rule({ h -> h.nationality == Nat.NORWEGIAN }, { h -> h.position == 1 }),
        PositionRule({ h -> h.smoking == Cigarette.CHESTERFIELD }, { i -> listOf(i + 1, i - 1) }, { h -> h.pet == Pet.FOX }),
        PositionRule({ h -> h.smoking == Cigarette.KOOLS }, { i -> listOf(i + 1, i - 1) }, { h -> h.pet == Pet.HORSE }),
        Rule({ h -> h.smoking == Cigarette.LUCKY_STRIKE }, { h -> h.bevarage == Drink.ORANGE_JUICE }),
        Rule({ h -> h.nationality == Nat.JAPANESE }, { h -> h.smoking == Cigarette.PARLAMENT }),
        PositionRule({ h -> h.nationality == Nat.NORWEGIAN }, { i -> listOf(i + 1, i - 1) }, { h -> h.color == Color.BLUE }),
    )

    val solution: List<House> by lazy { solve() }

    private fun solve(): List<House> {
        data class Solution(
            val positions: Set<Int>,
            val colors: Set<Color>,
            val nationalities: Set<Nat>,
            val pets: Set<Pet>,
            val drinks: Set<Drink>,
            val cigarretes: Set<Cigarette>,

            val houses: List<House>
        )

        fun incrementedSolutions(s: Solution): Sequence<Solution> {
            return sequence {
                for (pos in s.positions) {
                    for (color in s.colors) {
                        for (nat in s.nationalities) {
                            for (pet in s.pets) {
                                for (bevarage in s.drinks) {
                                    for (cigarette in s.cigarretes) {
                                        val newSolution = Solution(
                                            s.positions - pos,
                                            s.colors - color,
                                            s.nationalities - nat,
                                            s.pets - pet,
                                            s.drinks - bevarage,
                                            s.cigarretes - cigarette,
                                            s.houses + House(pos, color, nat, pet, bevarage, cigarette)
                                        )
                                        yield(newSolution)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fun iterate(solution: Solution): Solution? {
            for (s in incrementedSolutions(solution)) {
                // if all rules satisfy this (partial) solution
                if (rules.all { rule -> rule.isValid(s.houses) }) {
                    if (s.houses.size == 5) {
                        // found THE solution!
                        return s
                    } else {
                        // continue adding houses
                        val result = iterate(s)
                        if (result != null) {
                            return result
                        }
                    }
                } // else: skip this (partial) solution
            }
            return null
        }

        val initialSolution = Solution(
            (1..5).toSet(),
            Color.entries.toSet(),
            Nat.entries.toSet(),
            Pet.entries.toSet(),
            Drink.entries.toSet(),
            Cigarette.entries.toSet(),
            listOf()
        )

        return iterate(initialSolution)?.houses ?: throw IllegalStateException("I did something wrong")
    }

    fun drinksWater(): String {
        return solution.filter { h -> h.bevarage == Drink.WATER }.first().nationality.name
    }

    fun ownsZebra(): String {
        return solution.filter { h -> h.pet == Pet.ZEBRA }.first().nationality.name
    }
}

fun main() {
    val zebraPuzzle = ZebraPuzzle()
    println(zebraPuzzle.solution)
    println(zebraPuzzle.drinksWater())
    println(zebraPuzzle.ownsZebra())
}
