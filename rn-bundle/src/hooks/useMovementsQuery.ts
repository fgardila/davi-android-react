import { nativeApi } from "../services/nativeApi"
import { useQuery } from "@tanstack/react-query"

export const MOVEMENTS_KEY = ['movements'] as const;

export const useMovementsQuery = (size = 20) => {
    return useQuery({
        queryKey: [...MOVEMENTS_KEY, size],
        queryFn: async () => {
            const res = await nativeApi.getMovements(0, size)
            return res.items
        },
        staleTime: 5 * 60 * 1000,
    })
}
