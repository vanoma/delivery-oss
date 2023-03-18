class Storage {
    private prefix: string;

    constructor(prefix: string) {
        this.prefix = prefix;
    }

    private addPrefix(key: string): string {
        return this.prefix + key.charAt(0).toUpperCase() + key.substring(1);
    }

    public setItem(key: string, value: string): void {
        localStorage.setItem(this.addPrefix(key), value);
    }

    public getItem(key: string): string | null {
        // New code uses a prefix to save vanoma keys to local storage
        let value: string | null = localStorage.getItem(this.addPrefix(key));
        if (value !== null) {
            return value;
        }

        // Old code did not use a prefix for vanoma keys. If such key exists in local
        // storage, migrate it to the new code format and return its value
        value = localStorage.getItem(key);
        if (value !== null) {
            this.setItem(key, value);
            localStorage.removeItem(key);
            return value;
        }

        return value;
    }

    public removeItem(key: string): void {
        localStorage.removeItem(this.addPrefix(key));
    }

    public clear(): void {
        Object.keys(localStorage)
            .filter((key) => key.startsWith(this.prefix))
            .forEach((key) => localStorage.removeItem(key));
    }
}

export default new Storage('vanoma');
