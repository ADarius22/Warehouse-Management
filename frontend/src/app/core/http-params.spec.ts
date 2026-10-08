import { toParams } from './http-params';

describe('toParams', () => {
  it('keeps real values and converts them to strings', () => {
    const params = toParams({ search: 'bin', page: 0, active: false });

    expect(params.get('search')).toBe('bin');
    expect(params.get('page')).toBe('0');
    expect(params.get('active')).toBe('false');
  });

  it('skips null, undefined and empty strings', () => {
    const params = toParams({ a: null, b: undefined, c: '', d: 'x' });

    expect(params.keys()).toEqual(['d']);
  });
});
