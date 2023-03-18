# web-app

## Installing dependencies
```bash
yarn
```

## Development server
```bash
yarn dev
```

## Staging builds
```bash
yarn build:staging
```

## Production builds
```bash
yarn build:production
```

## Structure

1. **Components**: A component has one single purpose and can have an isolated state
2. **Features**: A feature represents a combination of components which implements a specific functional feature. A feature can have one an isolated state.
3. **Pages**: A page represents a combination of features and can have an isolated state. Users navigate through pages.
4. **Layouts**: Represents static aspect of pages that never changes.
