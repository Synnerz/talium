package com.github.synnerz.talium.shaders

import com.github.synnerz.talium.shaders.uniform.supportsShaders
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL20

/**
 * Taken from Elementa under MIT License
 * [Link](https://github.com/EssentialGG/Elementa/blob/c8cb78334a073ca4554cb74d49a771e5320351c5/src/main/kotlin/club/sk1er/elementa/shaders/Shader.kt)
 */
open class Shader(
    private val vertSource: String,
    private val fragSource: String
) {
    private var vertShader: Int = 0
    private var fragShader: Int = 0
    private var program: Int = 0
    /**
     * * Whether this [Shader] is usable or not
     * * This gets set to `true` whenever the shader is properly created otherwise it stays as `false`
     */
    var usable = false

    init {
        if (!vertSource.contains("void main()") || !fragSource.contains("void main()")) {
            println("Talium Shader: Invalid shader input.")
            throw Error("Please enter a valid Shader Source")
        }
        try {
            createShaders()
        } catch (e: Exception) {
            println("Talium Shader: Error whilst creating the shaders")
            e.printStackTrace()
        }
    }

    /**
     * * Binds this shader to the current rendering context
     */
    open fun bind() {
        if (!usable) return

        GL20.glUseProgram(program)
    }

    /**
     * * Unbinds this shader from the current rendering context
     */
    open fun unbind() {
        if (!usable) return

        GL20.glUseProgram(0)
    }

    /**
     * * Deletes this shader from the GPU memory
     * * Note: this sets the [usable] variable to false
     */
    open fun delete() {
        GL20.glDeleteProgram(program)
        usable = false
    }

    open fun getUniformLoc(name: String): Int {
        return if (supportsShaders) GL20.glGetUniformLocation(program, name)
        else ARBShaderObjects.glGetUniformLocationARB(program, name)
    }

    /**
     * * Used internally to create the shader on class initialization
     */
    private fun createShaders() {
        // Create the shader program and store its ID
        program = GL20.glCreateProgram()

        // Creating vertex shader
        vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        if (supportsShaders) GL20.glShaderSource(vertShader, vertSource)
        else ARBShaderObjects.glShaderSourceARB(vertShader, vertSource)
        GL20.glCompileShader(vertShader)

        if (GL20.glGetShaderi(vertShader, GL20.GL_COMPILE_STATUS) != 1) {
            println("Talium Shader Vertex: ${GL20.glGetShaderInfoLog(vertShader, 32768)}")
            return
        }

        // Creating fragment shader
        fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        if (supportsShaders) GL20.glShaderSource(fragShader, fragSource)
        else ARBShaderObjects.glShaderSourceARB(fragShader, fragSource)
        GL20.glCompileShader(fragShader)

        if (GL20.glGetShaderi(fragShader, GL20.GL_COMPILE_STATUS) != 1) {
            println("Talium Shader Fragment: ${GL20.glGetShaderInfoLog(fragShader, 32768)}")
            return
        }

        // Link programs
        GL20.glAttachShader(program, vertShader)
        GL20.glAttachShader(program, fragShader)
        GL20.glLinkProgram(program)

        if (supportsShaders) {
            GL20.glDetachShader(program, vertShader)
            GL20.glDetachShader(program, fragShader)
            GL20.glDeleteProgram(vertShader)
            GL20.glDeleteProgram(fragShader)
        } else {
            ARBShaderObjects.glDetachObjectARB(program, vertShader)
            ARBShaderObjects.glDetachObjectARB(program, fragShader)
            ARBShaderObjects.glDeleteObjectARB(vertShader)
            ARBShaderObjects.glDeleteObjectARB(fragShader)
        }

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) != 1) {
            println("Talium Shader Linking: ${GL20.glGetProgramInfoLog(program, 32768)}")
            return
        }

        // Validating program
        if (supportsShaders) GL20.glValidateProgram(program)
        else ARBShaderObjects.glValidateProgramARB(program)

        if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) != 1) {
            println("Talium Shader Validating: ${GL20.glGetProgramInfoLog(program, 32768)}")
            return
        }

        usable = true
    }

    companion object {
        private fun readFromResource(resourceName: String): String? {
            val normalized = resourceName.replace("\\", "/")
            val name = if (normalized.startsWith("/")) normalized else "/$normalized"
            val resourceStream = Companion::class.java.getResourceAsStream(name) ?: return null

            return resourceStream.bufferedReader().readText()
        }

        /**
         * * Makes a shader using the specified params to get the shaders from Talium's resources
         */
        fun fromResources(vertName: String, fragName: String) =
            Shader(
                readFromResource("assets/talium/shaders/${vertName}.vsh")!!,
                readFromResource("assets/talium/shaders/${fragName}.fsh")!!
            )
    }
}
