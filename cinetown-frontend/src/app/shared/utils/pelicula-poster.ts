const COLORES_POSTER = [
  ['#29334f', '#171e32'],
  ['#713632', '#351e24'],
  ['#28504c', '#142c2c'],
  ['#53364f', '#2b1b2c'],
  ['#39446c', '#202944'],
] as const;

export function obtenerFondoPoster(id: number): string {
  const [inicio, fin] = COLORES_POSTER[Math.abs(id) % COLORES_POSTER.length];
  return `linear-gradient(145deg, ${inicio}, ${fin})`;
}
