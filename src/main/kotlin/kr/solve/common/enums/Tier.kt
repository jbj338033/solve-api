package kr.solve.common.enums

enum class Tier(
    val minRating: Int,
    val maxRating: Int,
    val baseScore: Int,
) {
    MOON_5(0, 39, 20),
    MOON_4(40, 79, 60),
    MOON_3(80, 119, 100),
    MOON_2(120, 159, 140),
    MOON_1(160, 199, 180),
    STAR_5(200, 259, 230),
    STAR_4(260, 319, 290),
    STAR_3(320, 379, 350),
    STAR_2(380, 439, 410),
    STAR_1(440, 499, 470),
    COMET_5(500, 599, 550),
    COMET_4(600, 699, 650),
    COMET_3(700, 799, 750),
    COMET_2(800, 899, 850),
    COMET_1(900, 999, 950),
    PLANET_5(1000, 1099, 1050),
    PLANET_4(1100, 1199, 1150),
    PLANET_3(1200, 1299, 1250),
    PLANET_2(1300, 1399, 1350),
    PLANET_1(1400, 1499, 1450),
    NEBULA_5(1500, 1599, 1550),
    NEBULA_4(1600, 1699, 1650),
    NEBULA_3(1700, 1799, 1750),
    NEBULA_2(1800, 1899, 1850),
    NEBULA_1(1900, 1999, 1950),
    GALAXY_5(2000, 2099, 2050),
    GALAXY_4(2100, 2199, 2150),
    GALAXY_3(2200, 2299, 2250),
    GALAXY_2(2300, 2399, 2350),
    GALAXY_1(2400, 2499, 2450),
    UNIVERSE(2500, Int.MAX_VALUE, 2600),
    ;

    companion object {
        fun fromRating(rating: Int): Tier = entries.firstOrNull { rating in it.minRating..it.maxRating } ?: MOON_5
    }
}
