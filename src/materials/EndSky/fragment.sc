#ifndef INSTANCING
$input v_texcoord0, v_posTime
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;
  SAMPLER2D_AUTOREG(s_SkyTexture);
#endif

  
void main() {
  #ifndef INSTANCING
  nl_environment env = nlDetectEnvironment(FogColor.rgb, FogAndDistanceControl.xyz);
    vec4 diffuse = texture2D(s_SkyTexture, v_texcoord0);
    vec3 viewDir = normalize(v_posTime.xyz);
    vec3 color = renderEndSky(getEndHorizonCol(), getEndZenithCol(), normalize(v_posTime.xyz), v_posTime.w);
    color += 2.8*diffuse.rgb; // stars
    
    #ifdef NL_GALAXY_STARS
      color.rgb += 3.0*nlRenderGalaxy(viewDir, FogColor.rgb, env, v_posTime.w)*vec3(1.0, 2.0, 2.5);
    #endif
    color = colorCorrection(color);

    gl_FragColor = vec4(color, 1.0);
  #else
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
