package com.github.synnerz.talium.animations

import com.github.synnerz.talium.utils.MathLib
import kotlin.math.*

enum class Animations : IAnimation {
    LINEAR {
        override fun getEase(elapsedTime: Float): Float {
            return elapsedTime
        }
    },
    QUAD_IN {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowIn(elapsedTime, 2.0)
        }
    },
    QUAD_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowOut(elapsedTime, 2.0)
        }
    },
    QUAD_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowInOut(elapsedTime, 2.0)
        }
    },
    CUBIC_IN {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowIn(elapsedTime, 3.0)
        }
    },
    CUBIC_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowOut(elapsedTime, 3.0)
        }
    },
    CUBIC_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowInOut(elapsedTime, 3.0)
        }
    },
    QUART_IN {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowIn(elapsedTime, 4.0)
        }
    },
    QUART_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowOut(elapsedTime, 4.0)
        }
    },
    QUART_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowInOut(elapsedTime, 4.0)
        }
    },
    QUINT_IN {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowIn(elapsedTime, 5.0)
        }
    },
    QUINT_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowOut(elapsedTime, 5.0)
        }
    },
    QUINT_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getPowInOut(elapsedTime, 5.0)
        }
    },
    SINE_IN {
        override fun getEase(elapsedTime: Float): Float {
            return (1f - cos(elapsedTime * PI / 2f)).toFloat()
        }
    },
    SINE_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return sin(elapsedTime * PI / 2f).toFloat()
        }
    },
    SINE_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return (-0.5f * (cos(PI * elapsedTime) - 1f)).toFloat()
        }
    },
    BACK_IN {
        override fun getEase(elapsedTime: Float): Float {
            return (elapsedTime * elapsedTime * ((1.7f + 1f) * elapsedTime - 1.7f))
        }
    },
    BACK_OUT {
        override fun getEase(elapsedTime: Float): Float {
            var n = elapsedTime
            return (--n * n * ((1.7f + 1f) * n + 1.7f) + 1f)
        }
    },
    BACK_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getBackInOut(elapsedTime, 1.7f)
        }
    },
    CIRC_IN {
        override fun getEase(elapsedTime: Float): Float {
            return -(sqrt((1f - elapsedTime * elapsedTime)) - 1f)
        }
    },
    CIRC_OUT {
        override fun getEase(elapsedTime: Float): Float {
            var n = elapsedTime
            return sqrt(1f - (--n) * n)
        }
    },
    CIRC_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            var n = elapsedTime * 2f
            if (n < 1f) return (-0.5f * sqrt(1f * n * n) - 1f)

            n -= 2f
            return (0.5f * sqrt(1f - n * n) + 1f)
        }
    },
    BOUNCE_IN {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getBounceIn(elapsedTime)
        }
    },
    BOUNCE_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getBounceOut(elapsedTime)
        }
    },
    BOUNCE_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            if (elapsedTime < 0.5f) {
                return MathLib.getBounceIn(elapsedTime * 2f) * 0.5f
            }

            return MathLib.getBounceOut(elapsedTime * 2f - 1f) * 0.5f + 0.5f
        }
    },
    ELASTIC_IN {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getElasticIn(elapsedTime, 1.0, 0.3)
        }
    },
    ELASTIC_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getElasticOut(elapsedTime, 1.0, 0.3)
        }
    },
    ELASTIC_IN_OUT {
        override fun getEase(elapsedTime: Float): Float {
            return MathLib.getElasticInOut(elapsedTime, 1.0, 0.45)
        }
    },
    EASE_IN_EXPO {
        override fun getEase(elapsedTime: Float): Float {
            return (2f).pow(10f * (elapsedTime - 1f))
        }
    },
    EASE_OUT_EXPO {
        override fun getEase(elapsedTime: Float): Float {
            return -((2f).pow(-10f * elapsedTime) + 1f)
        }
    },
    EASE_IN_OUT_EXPO {
        override fun getEase(elapsedTime: Float): Float {
            var n = elapsedTime * 2
            if (n < 1f) return (2f).pow(10f * n - 1f) * 0.5f

            return (-(2f).pow(-10f * --n) + 2f) * 0.5f
        }
    }
}
