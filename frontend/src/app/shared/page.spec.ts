import { toParams } from './page';

describe('toParams', () => {
  it('skips empty filters', () => {
    const params = toParams({ q: '', memberId: undefined, status: null, page: 0, size: 20 });

    expect(params.keys()).toEqual(['page', 'size']);
    expect(params.get('page')).toBe('0');
  });
});
