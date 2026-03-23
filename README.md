# ☕ ByteBarista

> Brew blazing-fast binary codecs for your records — from `ByteBuffer` and `ByteArrayOutputStream` and back again.

---

### 🚀 What is ByteBarista?

**ByteBarista** is a high-performance, zero-reflection Java library for emitting `Codec`s that serialize and deserialize data directly into and out of:
- [`ByteBuffer`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/ByteBuffer.html)
- [`ByteArrayOutputStream`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/ByteArrayOutputStream.html)

It leverages modern Java features like:
- **Records** for type-safe schemas
- **Dynamic bytecode generation** for method-level performance
- **Unsigned support**, down to the bit
- Future-proofed for **Project Valhalla** and **value types**

---

### ✨ Features

- 🔧 Auto-generated `Codec`s for records with primitives, enums, and nested records
- ⚡ Fast, low-GC footprint: no reflection, no slow path
- 🧩 Support for custom annotations like `@UnsignedByte`, `@UnsignedShort`, `@UnsignedInteger`
- 🪄 Detects and optimizes enum storage size (`byte`, `short`, or `int`)
- 📤 Supports both **reading from** and **writing to** byte streams
- 🔍 Compact, maintainable design built for extension

---

### 📦 Getting Started

```java
// Example record
record PlayerState(int id, float health, boolean alive) {}

// Codec creation (under the hood generates specialized bytecode)
var codec = CodecRegistry.create(PlayerState.class);

// Encoding to bytes
var out = new ByteArrayOutputStream();
codec.write(out, new PlayerState(42, 92.5f, true));

// Decoding from bytes
ByteBuffer in = ByteBuffer.wrap(out.toByteArray());
var decoded = codec.read(in);
```