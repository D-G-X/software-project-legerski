import { defineConfig} from "orval";

export default defineConfig({
    'licensing-api': {
        output: {
            target: './app/services',
            schemas: './types',
            mode: 'tags-split',
            client: 'fetch',
            mock: true,
            clean: true
        },
        input: {
            target: '../resources/api.yaml'
        }
    }
})