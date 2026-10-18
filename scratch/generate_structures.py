import gzip
import os
import struct

# NBT Serializer
def write_tag(f, tag_type, name, payload):
    f.write(struct.pack('>b', tag_type))
    if name is not None:
        name_bytes = name.encode('utf-8')
        f.write(struct.pack('>h', len(name_bytes)))
        f.write(name_bytes)
    write_payload(f, tag_type, payload)

def write_payload(f, tag_type, payload):
    if tag_type == 1: # BYTE
        f.write(struct.pack('>b', payload))
    elif tag_type == 2: # SHORT
        f.write(struct.pack('>h', payload))
    elif tag_type == 3: # INT
        f.write(struct.pack('>i', payload))
    elif tag_type == 4: # LONG
        f.write(struct.pack('>q', payload))
    elif tag_type == 5: # FLOAT
        f.write(struct.pack('>f', payload))
    elif tag_type == 6: # DOUBLE
        f.write(struct.pack('>d', payload))
    elif tag_type == 8: # STRING
        b = payload.encode('utf-8')
        f.write(struct.pack('>h', len(b)))
        f.write(b)
    elif tag_type == 9: # LIST
        elem_type, elems = payload
        f.write(struct.pack('>b', elem_type))
        f.write(struct.pack('>i', len(elems)))
        for elem in elems:
            write_payload(f, elem_type, elem)
    elif tag_type == 10: # COMPOUND
        for child_type, child_name, child_payload in payload:
            write_tag(f, child_type, child_name, child_payload)
        f.write(struct.pack('>b', 0)) # TAG_End
    elif tag_type == 11: # INT_ARRAY
        f.write(struct.pack('>i', len(payload)))
        for val in payload:
            f.write(struct.pack('>i', val))

def build_structure(size_xyz, palette_names, block_data, output_path):
    palette_compounds = []
    for name in palette_names:
        # compound with Name
        palette_compounds.append([
            (8, "Name", name)
        ])

    blocks_compounds = []
    for pos, state_idx, nbt in block_data:
        b_entry = [
            (3, "state", state_idx),
            (9, "pos", (3, [pos[0], pos[1], pos[2]]))
        ]
        if nbt:
            b_entry.append((10, "nbt", nbt))
        blocks_compounds.append(b_entry)

    root_payload = [
        (9, "size", (3, [size_xyz[0], size_xyz[1], size_xyz[2]])),
        (9, "palette", (10, palette_compounds)),
        (9, "blocks", (10, blocks_compounds)),
        (9, "entities", (10, [])),
        (3, "DataVersion", 3465)
    ]

    import io
    buf = io.BytesIO()
    # Root compound has name ""
    write_tag(buf, 10, "", root_payload)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with gzip.open(output_path, 'wb') as gz:
        gz.write(buf.getvalue())
    print(f"Generated {output_path} ({len(block_data)} blocks)")


def make_box_structure(sx, sy, sz, floor_block, wall_block, corner_block, roof_block, chest_loot, accent_block, output_path):
    palette = [
        "minecraft:air",
        floor_block,
        wall_block,
        corner_block,
        roof_block,
        "minecraft:chest",
        accent_block,
        "minecraft:lantern"
    ]
    AIR, FLOOR, WALL, CORNER, ROOF, CHEST, ACCENT, LANTERN = range(8)

    blocks = []

    for x in range(sx):
        for z in range(sz):
            for y in range(sy):
                pos = (x, y, z)
                # 床 (y=0)
                if y == 0:
                    blocks.append((pos, FLOOR, None))
                # 屋根 (y=sy-1)
                elif y == sy - 1:
                    if x == 0 or x == sx - 1 or z == 0 or z == sz - 1:
                        blocks.append((pos, CORNER, None))
                    else:
                        blocks.append((pos, ROOF, None))
                # 外壁
                elif x == 0 or x == sx - 1 or z == 0 or z == sz - 1:
                    is_corner = (x == 0 or x == sx - 1) and (z == 0 or z == sz - 1)
                    # 入口 (z=0, xの中央2ブロック, y=1,2)
                    if z == 0 and (x == sx // 2 or x == (sx // 2) - 1) and y <= 2:
                        continue # 空洞 (入口)
                    # 窓 (y=2, 各壁の中央)
                    elif (x == sx // 2 or z == sz // 2) and y == 2 and not (z == 0):
                        blocks.append((pos, ACCENT, None))
                    elif is_corner:
                        blocks.append((pos, CORNER, None))
                    else:
                        blocks.append((pos, WALL, None))
                # 室内インテリア
                else:
                    # 中央奥のチェスト (x=sx//2, z=sz-2, y=1)
                    if x == sx // 2 and z == sz - 2 and y == 1:
                        chest_nbt = [
                            (8, "LootTable", chest_loot)
                        ]
                        blocks.append((pos, CHEST, chest_nbt))
                    # 天井のランタン
                    elif (x == sx // 2 or x == sx // 4 or x == 3 * sx // 4) and (z == sz // 2) and y == sy - 2:
                        blocks.append((pos, LANTERN, None))
                    # 四隅の柱アクセント
                    elif (x == 1 or x == sx - 2) and (z == 1 or z == sz - 2) and y == 1:
                        blocks.append((pos, ACCENT, None))

    build_structure((sx, sy, sz), palette, blocks, output_path)

if __name__ == "__main__":
    out_dir = r"c:\Users\might\Desktop\LegendMagic\src\main\resources\data\legendmagic\structures"

    # 1. ローランド王城砦 (Roland Fortress)
    make_box_structure(
        15, 8, 15,
        "minecraft:stone_bricks",
        "minecraft:stone_bricks",
        "minecraft:chiseled_stone_bricks",
        "minecraft:stone_brick_slab",
        "legendmagic:chests/roland_fortress",
        "minecraft:red_wool",
        os.path.join(out_dir, "roland_fortress.nbt")
    )

    # 2. エスタブール魔導研究所 (Estabul Lab)
    make_box_structure(
        13, 7, 13,
        "minecraft:smooth_quartz",
        "minecraft:lapis_block",
        "minecraft:prismarine_bricks",
        "minecraft:prismarine_brick_slab",
        "legendmagic:chests/estabul_lab",
        "minecraft:bookshelf",
        os.path.join(out_dir, "estabul_lab.nbt")
    )

    # 3. ルーナ神殿 (Runa Shrine)
    make_box_structure(
        13, 8, 13,
        "minecraft:smooth_quartz",
        "minecraft:quartz_pillar",
        "minecraft:chiseled_quartz_block",
        "minecraft:smooth_quartz_slab",
        "legendmagic:chests/runa_shrine",
        "minecraft:sea_lantern",
        os.path.join(out_dir, "runa_shrine.nbt")
    )

    # 4. ガスターク軍の砦 (Gastark Fortress)
    make_box_structure(
        15, 8, 15,
        "minecraft:polished_blackstone_bricks",
        "minecraft:polished_blackstone_bricks",
        "minecraft:crying_obsidian",
        "minecraft:polished_blackstone_brick_slab",
        "legendmagic:chests/gastark_fortress",
        "minecraft:gilded_blackstone",
        os.path.join(out_dir, "gastark_fortress.nbt")
    )
