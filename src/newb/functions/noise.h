#ifndef NOISE_H
#define NOISE_H

#include "constants.h"

// functions under [1] are from https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83

// [1] hash function for noise (for highp only)
float rand(highp vec2 n) {
  return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);
}

// 1D noise - used in plants,lantern wave
float noise1D(highp float x) {
  float x0 = floor(x);
  float t0 = x-x0;
  t0 *= t0*(3.0-2.0*t0);
  return mix(fract(sin(x0)*84.85), fract(sin(x0+1.0)*84.85), t0);
}

// simpler rand for disp, puddles
float fastRand(vec2 n){
  return fract(37.45*sin(dot(n, vec2(4.36, 8.28))));
}

// water displacement map (also used by caustic)
float disp(vec3 pos, float t) {
  float n = sin(8.0*NL_CONST_PI_HALF*(pos.x+pos.y*pos.z) + 0.7*t);
  pos.y += t + 0.8*n;
  float p = floor(pos.y);
  return (0.8+0.2*n) * mix(fastRand(pos.xz+p), fastRand(pos.xz+p+1.0), pos.y - p);
}

// [1]
float noise2D(vec2 u) {
  vec2 u0 = floor(u);
  vec2 v = u-u0;
  v *= v*(3.0 - 2.0*v);
  float c0 = rand(u0);
  float c1 = rand(u0+vec2(1.0, 0.0));
  float c2 = rand(u0+vec2(1.0, 1.0));
  float c3 = rand(u0+vec2(0.0, 1.0));
  return mix(mix(c0, c3, v.y), mix(c1, c2, v.y), v.x);
}

vec4 mod289(vec4 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 perm(vec4 x) {
  return mod289(((x * 34.0) + 1.0) * x);
}

// [1] used by galaxy
float noise3D(vec3 p){
  vec3 a = floor(p);
  vec3 d = p - a;
  //d = d * d * (3.0 - 2.0 * d);

  vec4 b = a.xxyy + vec4(0.0, 1.0, 0.0, 1.0);
  vec4 k1 = perm(b.xyxy);
  vec4 k2 = perm(k1.xyxy + b.zzww);

  vec4 c = k2 + a.zzzz;
  vec4 k3 = perm(c);
  vec4 k4 = perm(c + 1.0);
  vec4 o1 = fract(k3 / 41.0);
  vec4 o2 = fract(k4 / 41.0);
  vec4 o3 = o2 * d.z + o1 * (1.0 - d.z);
  vec2 o4 = o3.yw * d.x + o3.xz * (1.0 - d.x);

  return o4.y * d.y + o4.x * (1.0 - d.y);
}

float fastVoronoi2(vec2 pos, float f) {
  vec4 p = pos.xyxy;
  p.zw += p.wz*mix(vec2(0.895,-0.937), vec2(0.937,-0.698), vec2(0.698, -0.837));
  p = fract(p) - 0.5;
  p *= p;
  return 1.0-f*min(p.x+p.y, p.z+p.w);
}

float fastVoronoiCirrus(vec2 pos, float f) {
  vec4 p = pos.xyxy;
  p.zw += p.wz*mix(vec2(-0.15,-0.75), vec2(-1.06,0.95), vec2(0.99,-0.78));
  p.yz += p.xz*mix(vec2(-0.05,-1.05), vec2(-0.5,2.0), 1.0);
  p = fract(p) - 0.5;
  p *= p;
  return 1.0-f*min(p.x+p.y, p.z+p.w);
}

// Simple Perlin Noise
vec2 n22 (vec2 p)
{
    vec3 a = fract(p.xyx * vec3(123.34, 234.34, 345.65));
    a += dot(a, a + 34.45);
    return fract(vec2(a.x * a.y, a.y * a.z));
}

vec2 get_gradient(vec2 pos)
{
    float twoPi = 6.283185;
    float angle = n22(pos).x * twoPi;
    return vec2(cos(angle), sin(angle));
}

float perlin_noise(vec2 uv, float cells_count)
{
    vec2 pos_in_grid = uv * cells_count;
    vec2 cell_pos_in_grid =  floor(pos_in_grid);
    vec2 local_pos_in_cell = (pos_in_grid - cell_pos_in_grid);
    vec2 blend = local_pos_in_cell * local_pos_in_cell * (3.0 - 2.0 * local_pos_in_cell);

    vec2 left_top = cell_pos_in_grid + vec2(0, 1);
    vec2 right_top = cell_pos_in_grid + vec2(1, 1);
    vec2 left_bottom = cell_pos_in_grid + vec2(0, 0);
    vec2 right_bottom = cell_pos_in_grid + vec2(1, 0);

    float left_top_dot = dot(pos_in_grid - left_top, get_gradient(left_top));
    float right_top_dot = dot(pos_in_grid - right_top,  get_gradient(right_top));
    float left_bottom_dot = dot(pos_in_grid - left_bottom, get_gradient(left_bottom));
    float right_bottom_dot = dot(pos_in_grid - right_bottom, get_gradient(right_bottom));

    float noise_value = mix(
                            mix(left_bottom_dot, right_bottom_dot, blend.x),
                            mix(left_top_dot, right_top_dot, blend.x),
                            blend.y);


    return (0.5 + 0.5 * (noise_value / 0.7));
}


#endif
