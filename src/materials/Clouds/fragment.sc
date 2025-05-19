$input v_color0, v_color1, v_color2, v_fogColor
#include <newb/config.h>

#include <bgfx_shader.sh>
#include <newb/main.sh>


uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;

#define NL_CLOUD_PARAMS(x) NL_CLOUD2##x##STEPS, NL_CLOUD2##x##THICKNESS, NL_CLOUD2##x##RAIN_THICKNESS, NL_CLOUD2##x##VELOCITY, NL_CLOUD2##x##SCALE, NL_CLOUD2##x##DENSITY, NL_CLOUD2##x##SHAPE

void main() {
  vec4 color = v_color0;
  
    float day = pow(max(min(1.0 - FogColor.r * 1.2, 1.0), 0.0), 0.4);
  float night = pow(max(min(1.0 - FogColor.r * 1.5, 1.0), 0.0), 1.2);
  float dusk = max(FogColor.r - FogColor.b, 0.0);

  #if NL_CLOUD_TYPE >= 2
    vec3 vDir = normalize(v_color0.xyz);

    #if NL_CLOUD_TYPE == 2
      color = renderCloudsRounded(vDir, v_color0.xyz, v_color1.w, v_color2.w, v_color2.rgb, v_color1.rgb, NL_CLOUD_PARAMS(_));

      #ifdef NL_CLOUD2_LAYER2
        vec2 parallax = vDir.xz / abs(vDir.y) * NL_CLOUD2_LAYER2_OFFSET;
        vec3 offsetPos = v_color0.xyz;
        offsetPos.xz += parallax;
        vec4 color2 = renderCloudsRounded(vDir, offsetPos, v_color1.a, v_color2.a*2.0, v_color2.rgb, v_color1.rgb, NL_CLOUD_PARAMS(_LAYER2_));
        color = mix(color2, color, 0.2 + 0.8*color.a);
        
      #endif

      #ifdef NL_AURORA
        color += renderAurora(v_color0.xyz, v_color2.a, v_color1.a, v_fogColor)*(1.0-0.95*color.a);
      #endif
      
      color.a *= v_color0.a;
      if(vDir.y >= 0.0){
      color.a *= smoothstep(0.1, 0.6, vDir.y);
      } else {
      color.a *= smoothstep(-0.1, -0.6, vDir.y);
      }
      
    #else
      vDir.xz *= 0.3 + v_color0.w; // height parallax
     float a = 0.8; // or -ve
     float cosa = cos(a); float sina = sin(a);
      
      vec2 p = 3.3*vDir.xz/(0.08 + 0.25*abs(vDir.y));
           p = mul(p, mtxFromRows(vec2(cosa, sina), vec2(-sina, cosa)));
      vec4 clouds = renderClouds(p, v_color2.w, v_color1.w, v_color2.rgb, v_color1.rgb, NL_CLOUD3_SCALE, NL_CLOUD3_SPEED, NL_CLOUD3_SHADOW);
          float b = 0.8;
          float cosb = cos(b); float sinb = sin(b);
          
          vec2 pc = 3.5*vDir.xz/(0.01 + 0.5*abs(vDir.y));
           pc = mul(pc, mtxFromRows(vec2(cosb, sinb), vec2(-sinb, cosb)));
      vec4 cirrus = renderCloudCirrus(pc, v_color2.w, v_color1.w, v_color2.rgb, v_color1.rgb, NL_CLOUD3_SCALE, NL_CLOUD3_SPEED, 0.1);
      clouds = mix(cirrus, clouds, 0.3+0.8*clouds.a);
      vec3 additional = NL_DAWN_ZENITH_COL;
      additional *= max(0.0, 1.0)*dusk;
      clouds.rgb += additional;
      clouds.rgb *= 1.0-0.6*dusk;
      clouds.rgb *= 1.0+1.0*night;
      
      color = clouds;
      
      #ifdef NL_AURORA
        p.xy *= 34.7;
        color += renderAurora(p.xyy, v_color2.w, v_color1.w, v_fogColor)*(1.0-0.7*color.a);
      #endif

      color.a *= smoothstep(0.0, 0.6, vDir.y);
    #endif

    color.rgb = colorCorrection(color.rgb);
  #endif

  gl_FragColor = color;
}
