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
  
  vec3 hash(vec3 p) {
    p = vec3(dot(p, vec3(127.1, 311.7, 740.7)), dot(p, vec3(269.5, 183.3, 246.1)), dot(p, vec3(113.5, 271.9, 124.6)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0* f);
    return mix(mix(mix(dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(0.0, 0.0, 0.0)), dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(1.0, 0.0, 0.0)), u.x), mix(dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(0.0, 1.0, 0.0)), dot(hash(i + vec3(0.0,0.0, 0.0)), f - vec3(1.0, 1.0, 0.0)), u.x), u.y), mix(mix(dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(0.0, 0.0, 1.0)), dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(1.0, 0.0, 1.0)), u.x), mix(dot(hash(i + vec3(0.0, 1.0, 1.0)), f - vec3(0.0, 1.0, 1.0)), dot(hash(i + vec3(0.0, 1.0, 1.0)), f - vec3(1.0, 1.0, 1.0)), u.x), u.y), u.z);
}

  
void main() {
  #ifndef INSTANCING
  nl_environment env = nlDetectEnvironment(FogColor.rgb, FogAndDistanceControl.xyz);
    vec4 diffuse = texture2D(s_SkyTexture, v_texcoord0);
    vec3 viewDir = normalize(v_posTime.xyz);
    vec3 color = renderEndSky(getEndHorizonCol(), getEndZenithCol(), normalize(v_posTime.xyz), v_posTime.w);
    color += 2.8*diffuse.rgb; // stars
    
    vec3 stars_direction = normalize(v_posTime.xyz);
    float stars_threshold = 9.0;
    float stars_exposure = 100.0;
    float stars = pow(clamp(noise(stars_direction * 200.0), 0.0, 1.0), stars_threshold) * stars_exposure;
    stars *= mix(0.4, 1.4, noise(stars_direction * 100.0 + vec3_splat(v_posTime.w)*2.0));

    vec3 starnoise = vec3(stars,stars,stars)*vec3(3.0, 5.5, 5.0);//màu của sao
    //color.rgb += starnoise;
    
    #ifdef NL_GALAXY_STARS
      color.rgb += 3.0*nlRenderGalaxy(viewDir, FogColor.rgb, env, v_posTime.w)*vec3(1.0, 2.0, 2.5);
    #endif
    color = colorCorrection(color);

    gl_FragColor = vec4(color, 1.0);
  #else
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
