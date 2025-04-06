plugins {
    kotlin("jvm") apply false
    id("gg.essential.multi-version.root")
}

preprocess {
    val a = createNode("1.8.9-forge-1", 10809, "mcp")
    val b = createNode("1.8.9-forge-2", 10809, "mcp")

    a.link(b)
}